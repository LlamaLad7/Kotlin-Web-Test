When running, the server accepts 4 kinds of request:
* POST /signup
    ```json
    {
      "username": "myCoolUsername",
      "password": "myCoolPassword"
    }
    ```
* POST /login
    ```json
    {
      "username": "myCoolUsername",
      "password": "myCoolPassword"
    }
    ```
* GET /data
    ```json5
    {
        "sessionId": "the ID of your session", // Given to you by the server when you log in
        "session": {
            "username": "myCoolUsername",
            "expiry": { // Given to you by the server when you log in
                "seconds": 1588089309,
                "nanos": 895000000
            } 
        }
    }
    ```
* POST /data
    ```json5
        {
            "sessionId": "the ID of your session", // Given to you by the server when you log in
            "session": {
                "username": "myCoolUsername",
                "expiry": { // Given to you by the server when you log in
                    "seconds": 1588089309,
                    "nanos": 895000000
                } 
            },
            "data": "What you wish to set your data to"
        }
     ```
