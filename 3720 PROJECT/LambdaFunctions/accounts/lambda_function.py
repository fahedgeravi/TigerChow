# CPSC 3720
# Team 10 Account Lambda Handler

import json
import boto3
import logging
from datetime import datetime, timedelta, timezone
from decimal import Decimal
from botocore.exceptions import BotoCoreError, ClientError

# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

dynamodb = boto3.resource("dynamodb")
notification_table = dynamodb.Table("NotificationTypes")
sent_notification_table = dynamodb.Table("SentNotifications")
accounts_table = dynamodb.Table("Accounts")


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


def build_response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
        },
        "body": json.dumps(body, cls=DecimalEncoder),
    }


# Guest account intialization and ensurance
def ensure_guest_account():
    try:
        response = accounts_table.get_item(Key={"id": "guest"})
        if "Item" not in response:
            guest_account = {
                "id": "guest",
                "username": "guest",
                "notif_preference": "email",
                "inactive": False,
                "user_type": "guest",
                "email": "guest",
                "password": "",
                "last_login": datetime.now(timezone.utc).isoformat(),
            }
            accounts_table.put_item(Item=guest_account)
    except Exception as e:
        logger.error("Error ensuring guest account: %s", str(e))
        return build_response(500, {"message": "Internal server error"})


def lambda_handler(event, context):
    logger.info(f"Received event: {json.dumps(event)}")
    # Request data
    http_method = event.get("httpMethod")
    path = event.get("path")
    query_params = event.get("queryStringParameters", {})
    path_params = event.get("pathParameters", {})
    body = json.loads(event.get("body", "{}")) if event.get("body") else {}

    # Ensure the guest account exists
    ensure_guest_account()

    try:
        if path == "/account":
            if http_method == "GET":
                # Define valid account types and statuses
                VALID_ACCOUNT_TYPES = [
                    "restaraunt_employee",
                    "standard_user",
                    "business_user",
                    "customer_service_rep",
                    "delivery_driver",
                    "admin",
                    "guest",
                ]
                VALID_ACCOUNT_STATUSES = [True, False]

                # Validate query parameters
                if query_params:
                    if (
                        "acc_type" in query_params
                        and query_params["acc_type"] not in VALID_ACCOUNT_TYPES
                    ):
                        return build_response(400, {"message": "Invalid account type"})
                    if "inactive" in query_params:
                        # Convert query parameter to boolean
                        inactive_val = query_params["inactive"].lower()
                        if inactive_val not in ["true", "false"]:
                            return build_response(
                                400, {"message": "Invalid account status"}
                            )
                        acc_status = True if inactive_val == "true" else False

                # Handle recent login filtering
                if "last_login" in query_params:
                    try:
                        last_login_hours = int(query_params["last_login"])
                        if last_login_hours <= 0:
                            return build_response(
                                400,
                                {
                                    "message": "Invalid last_login value, must be a positive integer."
                                },
                            )
                    except ValueError:
                        return build_response(
                            400,
                            {
                                "message": "Invalid last_login paramenter. Must be an integer."
                            },
                        )

                    # Calculate time threshold
                    time_threshold = datetime.now(timezone.utc) - timedelta(
                        hours=last_login_hours
                    )
                    time_threshold_str = time_threshold.isoformat()

                # Prepare filter expression based on query parameters
                filter_expression = []
                expresion_values = {}
                if query_params:
                    if "inactive" in query_params:
                        filter_expression.append("inactive = :status")
                        expresion_values[":status"] = acc_status
                    if "acc_type" in query_params:
                        filter_expression.append("user_type = :type")
                        expresion_values[":type"] = query_params["acc_type"]
                    if "last_login" in query_params:
                        filter_expression.append("last_login >= :last_login")
                        expresion_values[":last_login"] = time_threshold_str
                # Join the filter expressions with AND
                if filter_expression:
                    filter_expression = " AND ".join(filter_expression)
                else:
                    filter_expression = None
                # Scan the table with the filter expression only if it exists
                if filter_expression:
                    scan_kwargs = {
                        "FilterExpression": filter_expression,
                        "ExpressionAttributeValues": expresion_values,
                    }
                else:
                    scan_kwargs = {}
                # Scan the accounts table
                response = accounts_table.scan(**scan_kwargs)
                items = response.get("Items", [])

                # Return 400 if no items were found
                if not items:
                    return build_response(400, {"message": "No accounts found"})

                return build_response(200, items)

            if http_method == "POST":
                VALID_ACCOUNT_TYPES = [
                    "restaraunt_employee",
                    "standard_user",
                    "business_user",
                    "customer_service_rep",
                    "delivery_driver",
                    "admin",
                    "guest",
                ]
                VALID_ACCOUNT_STATUSES = [True, False]
                VALID_NOTIFICATION_PREF = ["email", "text"]

                # Validate request body
                if (
                    not body
                    or "id" not in body
                    or "username" not in body
                    or "preferences" not in body
                    or "credentials" not in body
                ):
                    return build_response(
                        400, {"message": "Request body is missing or incomplete."}
                    )

                try:
                    user_id = body["id"]
                    username = body["username"]
                    notif_preference = body["preferences"]["notif_preference"]
                    user_type = body["preferences"]["user_type"]
                    inactive = body["preferences"]["inactive"]
                    email = body["credentials"]["email"]
                    password = body["credentials"]["password"]

                    # Check if the account already exists
                    existing_account = accounts_table.get_item(Key={"id": body["id"]})
                    if "Item" in existing_account:
                        return build_response(
                            400, {"message": "Account already exists."}
                        )

                    # Validate account type
                    if user_type not in VALID_ACCOUNT_TYPES:
                        return build_response(400, {"message": "Invalid account type."})
                    # Validate account status
                    if inactive not in VALID_ACCOUNT_STATUSES:
                        return build_response(
                            400, {"message": "Invalid account status."}
                        )
                    # Validate notification preference
                    if notif_preference not in VALID_NOTIFICATION_PREF:
                        return build_response(
                            400, {"message": "Invalid notification preference."}
                        )

                    item = {
                        "id": user_id,
                        "username": username,
                        "notif_preference": notif_preference,
                        "user_type": user_type,
                        "inactive": inactive,
                        "email": email,
                        "password": password,
                        "last_login": "0000-00T00:00:00",  # Placeholder for last login time
                    }

                    accounts_table.put_item(Item=item)
                    return build_response(
                        200, {"message": "Account created successfully."}
                    )
                except Exception as e:
                    logger.error("Error creating user: %s", str(e))
                    return build_response(500, {"message": "Internal server error"})

            if http_method == "DELETE":
                VALID_ACCOUNT_TYPES = [
                    "restaraunt_employee",
                    "standard_user",
                    "business_user",
                    "customer_service_rep",
                    "delivery_driver",
                    "admin",
                    "guest",
                ]

                # Delete all accounts of a specific type
                account_type = query_params.get("acc_type") if query_params else None
                if account_type:
                    if account_type not in VALID_ACCOUNT_TYPES:
                        return build_response(400, {"message": "Invalid account type"})
                    # Scan the table for items with the specified account type
                    response = accounts_table.scan(
                        FilterExpression="user_type = :type",
                        ExpressionAttributeValues={":type": account_type},
                    )
                    items = response.get("Items", [])
                    if not items:
                        return build_response(
                            400, {"message": "No accounts found for the specified type"}
                        )

                    # Delete the items
                    with accounts_table.batch_writer() as batch:
                        for item in items:
                            batch.delete_item(Key={"id": item["id"]})
                    # Ensure the guest account is recreated
                    ensure_guest_account()
                    return build_response(
                        200,
                        {
                            "message": f"All {account_type} accounts deleted successfully"
                        },
                    )

                # Delete all accounts
                response = accounts_table.scan()
                items = response.get("Items", [])
                if not items:
                    return build_response(400, {"message": "No accounts found"})
                with accounts_table.batch_writer() as batch:
                    for item in items:
                        batch.delete_item(Key={"id": item["id"]})
                # Ensure the guest account is recreated
                ensure_guest_account()
                return build_response(
                    200, {"message": "All accounts deleted successfully"}
                )

        elif path == "/account/login":
            if http_method == "POST":
                if not body or "email" not in body:
                    return build_response(
                        400, {"message": "Request body is missing or no email provided"}
                    )

                email = body["email"]

                # Guest account login
                if email == "guest":
                    # Return a guest account
                    response = accounts_table.get_item(Key={"id": "guest"})
                    if "Item" not in response:
                        return build_response(
                            400, {"message": "Guest account not available"}
                        )
                    # Update last login time
                    accounts_table.update_item(
                        Key={"id": "guest"},
                        UpdateExpression="SET last_login = :time",
                        ExpressionAttributeValues={":time": datetime.now().isoformat()},
                    )
                    return build_response(
                        200, {"message": "Guest user succefully signed in."}
                    )

                # Registered user login
                try:
                    response = accounts_table.scan(
                        FilterExpression="email = :email",
                        ExpressionAttributeValues={":email": email},
                    )
                    items = response.get("Items", [])
                    if not items:
                        return build_response(
                            400, {"message": "Invalid email or password"}
                        )

                    user = items[0]

                    if user.get("password") != body.get("password"):
                        return build_response(
                            400, {"message": "Invalid email or password"}
                        )

                    # Update last login time
                    accounts_table.update_item(
                        Key={"id": user["id"]},
                        UpdateExpression="SET last_login = :time",
                        ExpressionAttributeValues={":time": datetime.now().isoformat()},
                    )
                    return build_response(
                        200, {"message": "Registered user logged in succesfully."}
                    )
                except Exception as e:
                    logger.error("Error during login: %s", str(e))
                    return build_response(500, {"message": "Internal server error"})

        elif "id" in path_params:
            # Get the id and store in variable
            account_id = str(path_params.get("id"))

            if http_method == "GET":
                response = accounts_table.get_item(Key={"id": account_id})
                if "Item" in response:
                    user = response["Item"]
                    return build_response(200, user)
                else:
                    return build_response(400, {"message": "User not found"})

            if http_method == "PATCH":
                try:
                    accounts_table.update_item(
                        Key={"id": account_id},
                        UpdateExpression=f"SET {body['updateKey']} = :value",
                        ExpressionAttributeValues={":value": body["updateValue"]},
                    )
                    return build_response(
                        200, {"message": "Account updated successfully"}
                    )
                except Exception as e:
                    logger.error("Error editing user: %s", str(e))
                    return build_response(500, {"message": "Internal server error"})

            if http_method == "DELETE":
                try:
                    response = accounts_table.get_item(Key={"id": account_id})
                    if "Item" not in response:
                        return build_response(400, {"message": "User not found"})
                    
                    accounts_table.delete_item(Key={"id": account_id})
                    return build_response(200, {"message": "User deleted successfully"})
                except Exception as e:
                    logger.error("Error deleting user: %s", str(e))
                    return build_response(500, {"message": "Internal server error"})

        else:
            return build_response(404, {"message": "Resource not found"})

    except (BotoCoreError, ClientError) as e:
        logger.error("AWS error: %s", str(e))
        return {"statusCode": 500, "body": json.dumps({"message": str(e)})}
