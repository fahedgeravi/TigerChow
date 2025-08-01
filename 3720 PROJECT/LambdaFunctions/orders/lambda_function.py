# CPSC 3720
# Team 10 Order Lambda Handler

from random import randint
import uuid
import json
import boto3
import logging
import urllib3

# from botocore.vendored import requests
from datetime import datetime, timedelta, timezone
from decimal import Decimal

REQUEST_METHOD = {
    "POST": "POST",
    "GET": "GET",
    "DELETE": "DELETE",
    "PATCH": "PATCH",
}

STATUS_CODE = {
    "SUCCESS": 200,
    "CREATED": 201,
    "BAD_REQUEST": 400,
    "NOT_FOUND": 404,
    "NOT_SUPPORTED": 405,
    "INTERNAL_ERROR": 500,
}

ORDER_STATUS = {
    "created": "1",
    "being_prepared": "2",
    "on_the_way": "3",
    "ready_for_pickup": "8",
    "cancelled": "9",
    "delivered": "10",
}

NOTIFICATION_URL = (
    "https://a8reowdxxe.execute-api.us-east-1.amazonaws.com/dev/notification/"
)
ACCOUNT_URL = "https://kma3fle0n0.execute-api.us-east-1.amazonaws.com/dev/account/"

http = urllib3.PoolManager()

logger = logging.getLogger()
logger.setLevel(logging.INFO)

dynamodb = boto3.resource("dynamodb")
notification_table = dynamodb.Table("NotificationTypes")
sent_notification_table = dynamodb.Table("SentNotifications")
accounts_table = dynamodb.Table("Accounts")
orders_table = dynamodb.Table("Orders")


class DecimalEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Decimal):
            return int(obj) if obj % 1 == 0 else float(obj)
        return super(DecimalEncoder, self).default(obj)


def convert_decimals(obj):
    if isinstance(obj, list):
        return [convert_decimals(i) for i in obj]
    elif isinstance(obj, dict):
        return {k: convert_decimals(v) for k, v in obj.items()}
    elif isinstance(obj, Decimal):
        return int(obj) if obj % 1 == 0 else float(obj)
    return obj


def get_method_for_user(user_id: str) -> str | None:
    notif_headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
    }

    response = http.request("GET", f"{ACCOUNT_URL}{user_id}", headers=notif_headers)
    if response.status != 200:
        return None

    return json.loads(response.data)["notif_preference"]


def send_notification(notif_type: str, user_id: str):
    method = get_method_for_user(user_id) or "SMS"
    time_created = datetime.now().replace(microsecond=0).isoformat()
    notif_payload = json.dumps(
        {
            "notif_id": str(uuid.uuid4()),
            "method": method,
            "notif_type": notif_type,
            "id": user_id,
            "time_sent": time_created,
        }
    )
    notif_headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
    }

    return http.request(
        "POST", NOTIFICATION_URL, headers=notif_headers, body=notif_payload
    )


def lambda_handler(event, context):

    logger.info(f"Received event: {json.dumps(event)}")

    http_method = event.get("httpMethod")
    path = event.get("resource")
    path_params = event.get("pathParameters")
    query_params = event.get("queryStringParameters", {})
    body = json.loads(event.get("body", "{}")) if event.get("body") else {}

    try:
        if (path == "/order") and (path_params is None):
            # /order/
            if http_method == REQUEST_METHOD["GET"]:
                return order_get(query_params)
            elif http_method == REQUEST_METHOD["POST"]:
                return order_post(body)
            elif http_method == REQUEST_METHOD["DELETE"]:
                return order_delete(query_params)
            else:
                return build_response(STATUS_CODE["NOT_SUPPORTED"])

        elif path == r"/order/{order_id}":
            # /order/:order_id
            if http_method == REQUEST_METHOD["GET"]:
                return order_id_get(path_params)
            elif http_method == REQUEST_METHOD["PATCH"]:
                return order_id_patch(path_params, body)
            elif http_method == REQUEST_METHOD["DELETE"]:
                return order_id_delete(path_params)
            else:
                return build_response(STATUS_CODE["NOT_SUPPORTED"])

        else:
            return build_response(STATUS_CODE["NOT_SUPPORTED"])

    except Exception as e:
        print(e)
        return build_response(
            STATUS_CODE["INTERNAL_ERROR"],
            {"message": "Internal server error: " + str(e)},
        )


def build_response(status_code, body={}):
    logger.info(f"Response status: {status_code}\nResponse body {body}")
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
        },
        "body": json.dumps(body, cls=DecimalEncoder),
    }


def order_get(query_params):
    """
    Retrieve all orders, then filter in‑Python by:
      • user_id (exact match)
      • time_created in hours
      • restaurant_name (exact match)
      • status (exact match)
    """
    try:
        user_id = query_params.get("user_id")
        tc_filter = query_params.get("time_created")
        restaurant_name = query_params.get("restaurant_name")
        order_status = query_params.get("order_status")

        if tc_filter:
            try:
                filter_hours = int(tc_filter)
                if filter_hours <= 0:
                    return build_response(
                        STATUS_CODE["NOT_FOUND"],
                        {
                            "message": "Invalid time_created value, must be a positive integer."
                        },
                    )
            except ValueError:
                return build_response(
                    STATUS_CODE["NOT_FOUND"],
                    {"message": "Invalid user_id parameter. Must be an integer."},
                )

            time_threshold = datetime.now(timezone.utc) - timedelta(hours=filter_hours)
            time_threshold_str = time_threshold.isoformat()

        filter_expression = []
        expresion_values = {}

        if "user_id" in query_params:
            filter_expression.append("user_id = :id")
            expresion_values[":id"] = user_id
        if "time_created" in query_params:
            filter_expression.append("time_created >= :time_created")
            expresion_values[":time_created"] = time_threshold_str
        if "restaurant_name" in query_params:
            filter_expression.append("restaurant = :restaurant_name")
            expresion_values[":restaurant_name"] = restaurant_name
        if "order_status" in query_params:
            filter_expression.append("order_status = :order_status")
            expresion_values[":order_status"] = order_status

        if filter_expression:
            filter_expression = " AND ".join(filter_expression)
        else:
            filter_expression = None

        if filter_expression:
            scan_kwargs = {
                "FilterExpression": filter_expression,
                "ExpressionAttributeValues": expresion_values,
            }
        else:
            scan_kwargs = {}

        response = orders_table.scan(**scan_kwargs)
        items = response.get("Items", [])

        # Return 400 if no items were found
        if not items:
            return build_response(
                STATUS_CODE["NOT_FOUND"], {"message": "No orders found"}
            )

        return build_response(STATUS_CODE["SUCCESS"], items)

    except Exception as e:
        logger.error(f"order_get failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})


def order_delete(query_params):
    """
    Delete all orders that match the same in‑Python filters as order_get.
    """
    try:
        user_id = query_params.get("user_id")
        tc_filter = query_params.get("time_created")
        restaurant_name = query_params.get("restaurant_name")
        order_status = query_params.get("order_status")

        if tc_filter:
            try:
                filter_hours = int(tc_filter)
                if filter_hours <= 0:
                    return build_response(
                        STATUS_CODE["NOT_FOUND"],
                        {
                            "message": "Invalid time_created value, must be a positive integer."
                        },
                    )
            except ValueError:
                return build_response(
                    STATUS_CODE["NOT_FOUND"],
                    {"message": "Invalid user_id parameter. Must be an integer."},
                )

            time_threshold = datetime.now(timezone.utc) - timedelta(hours=filter_hours)
            time_threshold_str = time_threshold.isoformat()

        filter_expression = []
        expresion_values = {}

        if "user_id" in query_params:
            filter_expression.append("user_id = :id")
            expresion_values[":id"] = user_id
        if "time_created" in query_params:
            filter_expression.append("time_created >= :time_created")
            expresion_values[":time_created"] = time_threshold_str
        if "restaurant_name" in query_params:
            filter_expression.append("restaurant = :restaurant_name")
            expresion_values[":restaurant_name"] = restaurant_name
        if "order_status" in query_params:
            filter_expression.append("order_status = :order_status")
            expresion_values[":order_status"] = order_status

        if filter_expression:
            filter_expression = " AND ".join(filter_expression)
        else:
            filter_expression = None

        if filter_expression:
            scan_kwargs = {
                "FilterExpression": filter_expression,
                "ExpressionAttributeValues": expresion_values,
            }
        else:
            scan_kwargs = {}

        response = orders_table.scan(**scan_kwargs)
        items = response.get("Items", [])

        # Return 400 if no items were found
        if not items:
            return build_response(
                STATUS_CODE["NOT_FOUND"], {"message": "No orders found"}
            )
        else:
            for item in items:
                orders_table.delete_item(Key={"order_id": item["order_id"]})

        return build_response(
            STATUS_CODE["SUCCESS"], {"message": "Orders deleted successfully"}
        )

    except Exception as e:
        logger.error(f"order_delete failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})


def order_post(body):
    """
    Create a new order with user_id and items, save to DynamoDB.
    """
    try:
        user_id = body.get("user_id")
        items = body.get("items")

        if not user_id:
            return build_response(
                STATUS_CODE["BAD_REQUEST"],
                {"message": "Missing required fields: user_id"},
            )
        if not items:
            return build_response(
                STATUS_CODE["BAD_REQUEST"],
                {"message": "Missing required fields: items"},
            )

        order_id = str(uuid.uuid4())
        time_created = datetime.now().replace(microsecond=0).isoformat()

        total_price = 0
        for i, item in enumerate(items):
            try:
                total_price += float(item["price"].replace("$", "")) * int(
                    item["quantity"]
                )
            except (AttributeError, ValueError, KeyError):
                return build_response(
                    STATUS_CODE["BAD_REQUEST"],
                    {"message": f"Item at index {i} contained invalid price"},
                )
        total_price = str(total_price.__round__(2))

        order_status = "created"
        restaurant_name = body.get("restaurant_name")

        new_order = {
            "order_id": order_id,
            "user_id": user_id,
            "restaurant": restaurant_name,
            "items": items,
            "time_created": time_created,
            "total_price": total_price,
            "order_status": order_status,
        }

        orders_table.put_item(Item=new_order)

        response = send_notification("1", user_id)
        if response.status != STATUS_CODE["CREATED"]:
            return build_response(
                STATUS_CODE["INTERNAL_ERROR"],
                {"message": f"Failed to send order notification, got {response.data}"},
            )

        return build_response(STATUS_CODE["CREATED"], new_order)

    except Exception as e:
        logger.error(f"order_post failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})


def order_id_get(path_params):
    try:
        order_id = str(path_params.get("order_id"))

        if not order_id:
            return build_response(
                STATUS_CODE["BAD_REQUEST"], {"message": "Missing order_id in path"}
            )

        response = orders_table.get_item(Key={"order_id": order_id})
        if "Item" in response:
            order = response["Item"]
            return build_response(STATUS_CODE["SUCCESS"], order)
        else:
            return build_response(
                STATUS_CODE["NOT_FOUND"], {"message": "Order not found"}
            )
    except Exception as e:
        logger.error(f"order_id_get failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})


def order_id_patch(path_params, body):
    try:
        order_id = path_params.get("order_id")
        update_key = body.get("updateKey")
        update_value = body.get("updateValue")

        if not order_id:
            return build_response(
                STATUS_CODE["BAD_REQUEST"], {"message": "Missing order_id in path"}
            )
        if not update_key:
            return build_response(
                STATUS_CODE["BAD_REQUEST"], {"message": "Missing updateKey in body"}
            )
        if update_value is None:
            return build_response(
                STATUS_CODE["BAD_REQUEST"], {"message": "Missing updateValue in body"}
            )

        existing = orders_table.get_item(Key={"order_id": order_id})
        if "Item" not in existing:
            return build_response(
                STATUS_CODE["NOT_FOUND"], {"message": f"Order {order_id} not found"}
            )
        user_id = existing["Item"]["user_id"]

        if update_key == "order_status":
            if update_value not in ORDER_STATUS.keys():
                return build_response(
                    STATUS_CODE["BAD_REQUEST"],
                    {
                        "message": f"Invalid status. Must be one of {list(ORDER_STATUS.keys())}"
                    },
                )
            expr_attr_names = {"#st": "order_status"}
            expr_attr_values = {":val": update_value}
            update_expr = "SET #st = :val"

        elif update_key == "items":
            if not isinstance(update_value, list):
                return build_response(
                    STATUS_CODE["BAD_REQUEST"], {"message": "items must be a list"}
                )
            total = 0.0
            for idx, item in enumerate(update_value):
                if not all(k in item for k in ("item_id", "name", "quantity", "price")):
                    return build_response(
                        STATUS_CODE["BAD_REQUEST"],
                        {
                            "message": f"Item at index {idx} missing one of item_id, name, quantity, price"
                        },
                    )
                try:
                    qty = int(item["quantity"])
                    price = float(item["price"])
                except (ValueError, TypeError):
                    return build_response(
                        STATUS_CODE["BAD_REQUEST"],
                        {
                            "message": f"Invalid quantity or price in item at index {idx}"
                        },
                    )
                total += qty * price

            total_str = f"{total:.2f}"
            expr_attr_names = {"#it": "items", "#tp": "total_price"}
            expr_attr_values = {":it": update_value, ":tp": total_str}
            update_expr = "SET #it = :it, #tp = :tp"

        else:
            placeholder = f"#{update_key}"
            expr_attr_names = {placeholder: update_key}
            expr_attr_values = {":val": update_value}
            update_expr = f"SET {placeholder} = :val"

        orders_table.update_item(
            Key={"order_id": order_id},
            UpdateExpression=update_expr,
            ExpressionAttributeNames=expr_attr_names,
            ExpressionAttributeValues=expr_attr_values,
            ReturnValues="ALL_NEW",
        )

        if update_key == "status":
            notif_type = ORDER_STATUS.get(update_value)
            send_notification(notif_type, user_id)

        updated_order = orders_table.get_item(Key={"order_id": order_id})["Item"]
        return build_response(STATUS_CODE["SUCCESS"], updated_order)

    except Exception as e:
        logger.error(f"order_id_patch failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})


def order_id_delete(path_params):
    """
    Delete a single order by order_id.
    """
    try:
        order_id = path_params.get("order_id")

        if not order_id:
            return build_response(
                STATUS_CODE["BAD_REQUEST"], {"message": "Missing order_id in path"}
            )

        response = orders_table.get_item(Key={"order_id": order_id})
        if "Item" not in response:
            return build_response(
                STATUS_CODE["NOT_FOUND"],
                {"message": f"No order found with ID {order_id}"},
            )

        orders_table.delete_item(Key={"order_id": order_id})

        return build_response(
            STATUS_CODE["SUCCESS"],
            {"message": f"Order {order_id} deleted successfully"},
        )

    except Exception as e:
        logger.error(f"order_id_delete failed: {e}")
        return build_response(STATUS_CODE["INTERNAL_ERROR"], {"message": str(e)})
