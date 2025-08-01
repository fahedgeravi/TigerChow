# Database Layout

---

## Accounts Table

Attributes:
- UserID (string)
- lastlogin (string of time)
- user type (string)
- registered/unregistered (boolean)
- user prefrences

## Notification Types Table
- id (string) (partition key)
- Description (string)
- Message (string)

## Notifications Table
- notif_id (string) (partition key)
- id (string)
- Notification Type (string)
- method (string)
- Time Sent (string)
