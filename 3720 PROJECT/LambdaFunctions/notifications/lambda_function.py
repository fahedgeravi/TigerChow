# CPSC 3720
# Team 10 Notification Lambda Handler

import json
import boto3
import logging
from typing import Optional
from datetime import datetime
from decimal import Decimal
from botocore.exceptions import BotoCoreError, ClientError

# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

dynamodb = boto3.resource("dynamodb")
notification_table = dynamodb.Table("NotificationTypes")
sent_notification_table = dynamodb.Table("SentNotifications")
accounts_table = dynamodb.Table("Accounts")


# Custom JSON encoder for Decimal objects
class DecimalEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Decimal):
            # Convert Decimal to float or int
            return int(obj) if obj % 1 == 0 else float(obj)
        return super(DecimalEncoder, self).default(obj)


def convert_decimals(obj):
    if isinstance(obj, list):
        return [convert_decimals(i) for i in obj]
    elif isinstance(obj, dict):
        return {k: convert_decimals(v) for k, v in obj.items()}
    elif isinstance(obj, Decimal):
        return (
            int(obj) if obj % 1 == 0 else float(obj)
        )  # Convert to int if it's a whole number, else float
    return obj


def get_notification_type_id(description: str) -> Optional[int]:
    """Retrieve notification type ID by searching for its description."""
    try:
        response = notification_table.scan(
            FilterExpression="description = :desc",
            ExpressionAttributeValues={":desc": description},
        )
        items = response.get("Items", [])
        return items[0]["id"] if items else None
    except (BotoCoreError, ClientError) as e:
        logger.error("AWS error: %s", str(e))
        return None


def build_response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
        },
        "body": json.dumps(body, cls=DecimalEncoder),
    }


def lambda_handler(event, context):
    logger.info(f"Received event: {json.dumps(event)}")
    # Request data
    http_method = event.get("httpMethod")
    path = event.get("path")
    query_params = event.get("queryStringParameters", {})
    body = json.loads(event.get("body", "{}")) if event.get("body") else {}

    try:
        if path == "/notification/type":
            if http_method == "GET":
                # Prepare filter expressions based on provided query parameters
                filter_expressions = []
                expression_values = {}
                if query_params:
                    if "type" in query_params:
                        # Use an expression attribute name (#type) to avoid conflicts with reserved keywords
                        filter_expressions.append("#type = :type")
                        expression_values[":type"] = query_params["type"]
                    if "description" in query_params:
                        filter_expressions.append("description = :description")
                        expression_values[":description"] = query_params["description"]
                    if "message" in query_params:
                        filter_expressions.append("message = :message")
                        expression_values[":message"] = query_params["message"]
                # Combine multiple filters using AND
                filter_expression = (
                    " AND ".join(filter_expressions) if filter_expressions else None
                )
                # Construct scan arguments only if filters exist
                scan_kwargs = (
                    {
                        "FilterExpression": filter_expression,
                        "ExpressionAttributeValues": expression_values,
                    }
                    if filter_expressions
                    else {}
                )
                # Execute scan operation on the notification table
                response = notification_table.scan(**scan_kwargs)
                # return {"statusCode": 200, "body": json.dumps(response.get("Items", []))}
                return build_response(200, response.get("Items", []))

            elif http_method == "POST":
                if "id" not in body or "type" not in body or "message" not in body:
                    # return {"statusCode": 400, "body": json.dumps({"message": "Missing required fields for new notification"})}
                    return build_response(
                        400, {"message": "Missing required fields for new notification"}
                    )
                # Insert a new notification type into the table
                notification_table.put_item(Item=body)
                # return {"statusCode": 201, "body": json.dumps({"message": "Notification type created"})}
                return build_response(201, {"message": "Notification type created"})

            elif http_method == "PATCH":
                # Validate required fields for updating a notification
                if "id" not in body or "key" not in body or "value" not in body:
                    # return {"statusCode": 400, "body": json.dumps({"message": "Missing required fields for update"})}
                    return build_response(
                        400, {"message": "Missing required fields for update"}
                    )
                # Check if the notification type exists before updating
                existing_item = notification_table.get_item(Key={"id": body["id"]}).get(
                    "Item"
                )
                if not existing_item:
                    # return {"statusCode": 400, "body": json.dumps({"message": "Notification type not found"})}
                    return build_response(
                        400, {"message": "Notification type not found"}
                    )
                # Construct update expression dynamically
                update_expr = f"set {body['key']} = :val"
                notification_table.update_item(
                    Key={"id": body["id"]},
                    UpdateExpression=update_expr,
                    ExpressionAttributeValues={":val": body["value"]},
                )
                # return {"statusCode": 200, "body": json.dumps({"message": "Notification type updated"})}
                return build_response(200, {"message": "Notification type updated"})

            elif http_method == "DELETE":

                result = notification_table.scan()
                items = result.get("Items", [])

                if query_params:
                    for param in query_params:
                        items = [
                            item
                            for item in items
                            if item.get(param) == query_params[param]
                        ]

                # Perform delete operation on the notification type
                for item in items:
                    notification_table.delete_item(Key={"id": item.get("id")})
                # return {"statusCode": 200, "body": json.dumps({"message": "Notification type deleted"})}
                if len(items) > 1:
                    return build_response(
                        200, {"message": "Multiple Notification types deleted"}
                    )
                elif len(items) == 1:
                    return build_response(200, {"message": "Notification type deleted"})
                else:
                    return build_response(
                        400, {"message": "Notification type not found"}
                    )

        # GET all and DELETE all sent notifications
        elif path == "/notification/" or path == "/notification":
            if http_method == "POST":
                return handle_post_sent_notification(body)
            elif http_method == "GET":
                return handle_get_sent_notifications(query_params)
            elif http_method == "DELETE":
                return handle_delete_sent_notification(query_params)
            else:
                return build_response(
                    405, {"message": "Method Not Allowed on /notification/"}
                )
        else:
            print(event)
            return build_response(404, {"message": "Resource not found"})

    except (BotoCoreError, ClientError) as e:
        logger.error("AWS error: %s", str(e))
        return build_response(500, {"message": str(e)})
    except Exception as e:
        logger.error("Internal error: %s", str(e))
        return build_response(500, {"message": "Internal server error: " + str(e)})


# Handler for POST
def handle_post_sent_notification(body):
    required_keys = ["notif_id", "method", "notif_type", "id", "time_sent"]
    missing_keys = []

    for key in required_keys:
        if key not in body:
            missing_keys.append(key)

    if len(missing_keys) != 0:
        return build_response(
            400, {"message": f"Missing required fields: {''.join(missing_keys)}"}
        )

    account_id = body["id"]
    notif_id = body["notif_id"]
    notif_type = body["notif_type"]
    type_response = notification_table.get_item(Key={"id": notif_type})
    if "Item" not in type_response:
        return build_response(
            400, {"message": f"Notification type with id {notif_type} not found"}
        )
    time_sent = body["time_sent"]
    method = body["method"]
    message_text = type_response["Item"]["message"]

    # Putting in new sent notification record into our sentNotificstions table
    new_item = {
        "notif_id": notif_id,
        "id": account_id,
        "notif_type": notif_type,
        "time_sent": time_sent,
        "method": method,
    }
    sent_notification_table.put_item(Item=new_item)

    # Response Message
    response_message = f"UserID {account_id} was sent the message '{message_text}'"
    return build_response(201, {"message": response_message})


# Handler for GET /notification/{id} endpoint
def handle_get_sent_notifications(query_params):
    result = sent_notification_table.scan()
    items = result.get("Items", [])

    # - notif_id
    # - method
    # - notif_type
    # - id
    # - time_sent

    if query_params:
        for param in query_params:
            # type checking
            if param not in ["notif_id", "method", "notif_type", "id", "time_sent"]:
                return build_response(400, {"message": "Unknown query parameter"})
            if (param == "notif_id") and (not query_params["notif_id"].isdigit()):
                return build_response(400, {"message": "Incorrect type for notif_id"})
            if (param == "id") and (not query_params["id"].isdigit()):
                return build_response(400, {"message": "Incorrect type for id"})
            if param == "time_sent":
                # Example: time=before:2025-03-31T12:00:00
                direction, timestamp = query_params[param].split(":", 1)
                query_time = datetime.fromisoformat(timestamp)

                if direction == "before":
                    items = [
                        item
                        for item in items
                        if "time_sent" in item
                        and datetime.fromisoformat(item["time_sent"]) < query_time
                    ]
                elif direction == "after":
                    items = [
                        item
                        for item in items
                        if "time_sent" in item
                        and datetime.fromisoformat(item["time_sent"]) > query_time
                    ]
            else:
                items = [
                    item for item in items if item.get(param) == query_params[param]
                ]

    return build_response(200, items)


# Handler for DELETE /notification/{id} endpoint
def handle_delete_sent_notification(query_params):

    result = sent_notification_table.scan()
    items = result.get("Items", [])

    if query_params:
        for param in query_params:
            # type checking
            if param not in ["notif_id", "method", "notif_type", "id", "time_sent"]:
                return build_response(400, {"message": "Unknown query parameter"})
            if (param == "notif_id") and (not query_params["notif_id"].isdigit()):
                return build_response(400, {"message": "Incorrect type for notif_id"})
            if (param == "id") and (not query_params["id"].isdigit()):
                return build_response(400, {"message": "Incorrect type for id"})
            if param == "time_sent":
                # Example: time=before:2025-03-31T12:00:00
                direction, timestamp = query_params[param].split(":", 1)
                query_time = datetime.fromisoformat(timestamp)

                if direction == "before":
                    items = [
                        item
                        for item in items
                        if "time_sent" in item
                        and datetime.fromisoformat(item["time_sent"]) < query_time
                    ]
                elif direction == "after":
                    items = [
                        item
                        for item in items
                        if "time_sent" in item
                        and datetime.fromisoformat(item["time_sent"]) > query_time
                    ]
            else:
                items = [
                    item for item in items if item.get(param) == query_params[param]
                ]

    for item in items:
        notif_id = item.get("notif_id")
        sent_notification_table.delete_item(Key={"notif_id": notif_id})
    return build_response(201, {"message": "Notification deleted successfully"})
