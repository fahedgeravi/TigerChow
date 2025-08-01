# CPSC 3720
# Script used to fill NotificationTypes table

import requests
import json

url = "/notification/type"

payloads = [
json.dumps({
  "id": "1",
  "type": "Order Received",
  "message": "The resturant has received your order!"
}),
json.dumps({
  "id": "2",
  "type": "Food being prepared",
  "message": "The resturant is preparing your order!"
}),
json.dumps({
  "id": "3",
  "type": "Food is out for delievery",
  "message": "Your order is out for delivery!"
}),
json.dumps({
  "id": "4",
  "type": "Ticket Logged",
  "message": "Customer Support has recieved your ticket, a representative will be in contact shortly"
}),
json.dumps({
  "id": "5",
  "type": "Ticket under investigation",
  "message": "Customer Support is investigating your ticket, a representative will be in contact shortly"
}),
json.dumps({
  "id": "6",
  "type": "Ticket resolved",
  "message": "Customer Support marked your ticket as resolved"
}),
json.dumps({
  "id": "7",
  "type": "Password Update",
  "message": "Your account Password has been changed. If this was not you, please contact Customer Support immediately"
}),
json.dumps({
  "id": "8",
  "type": "Order Ready for Pickup",
  "message": "Your order is ready for picukup"
}),
json.dumps({
  "id": "9",
  "type": "Order Cancelled",
  "message": "Your order has been cancelled"
}),
json.dumps({
  "id": "10",
  "type": "Order Delivered",
  "message": "Your order has been delievered"
})
]

headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json'
}

for payload in payloads:
  response = requests.request("POST", url, headers=headers, data=payload)

  print(response.text)
