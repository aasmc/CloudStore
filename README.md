# CloudStore 
Yandex Backend School Test App.
This is a simple backend server written with Spring Boot. 

## Description
The server stores info about files and folders uploaded by a user through /imports endpoint.
A user can:
- upload info about files and folders
- delete a file or folder by id
- get info about a particular file or folder
- get info about updates performed to all files and folders within 24 hours of a specified date
- get info about all updates performed to a particular file or folder within specified range of time

## Implementation
- Main Framework: Spring Boot
- Database: PostgreSQL
- Connection pool: HikariCP
- Build tool: Gradle

## Starting the app
Clone the git directory.
Install docker and docker-compose.
To run the app locally use docker-compose: in your terminal go to the project's /docker-compose directory
and run:
```text
docker compose up
```
Docker will initialize and run PostgreSQL and CloudStore.

CloudStore's Dockerfile is in the project's /docker-image directory.

## Connecting to server (for backend school only)
The server runs on https://ignore-1903.usr.yandex-academy.ru on http://localhost:8080
To enable using server's localhost from remote host we need to run the following command:
```text
ssh -L 8080:localhost:8080 LOGIN@10.22.5.43
```
Where LOGIN is your remote server's login.

## Connecting on local machine
The app runs on http://localhost:8080

## Endpoints
### POST /imports
This endpoint allows a user to save filesystem elements. The app accepts JSON object of the form:

```json
    {
        "items": [
            {
                "type": "FOLDER",
                "id": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                "parentId": "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"
            },
            {
                "type": "FILE",
                "url": "/file/url1",
                "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                "size": 128
            },
            {
                "type": "FILE",
                "url": "/file/url2",
                "id": "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                "size": 256
            }
        ],
        "updateDate": "2022-02-02T12:00:00Z"
    }
```
- id should be unique and cannot be null
- only a FOLDER can be a parent of other items
- items of type FOLDER don't have url or size fields
- length of an url of a file must be under 256 chars long
- items can have no parent (i.e. parentId = null)
- date must be compliant with ISO 8601, e.g. "2022-05-28T21:12:01.000Z". 
- there must be no elements with equal ids in the same batch of imports

Response: HttpStatus.OK 200 means that the request has been handled successfully.
Response: HttpStatus.BAD_REQUEST 400 means that the input document's scheme is invalid
or that there were validation errors, e.g. invalid dates or FOLDER with url or size fields.

```json
{
  "code": 400,
  "message": "Validation Failed"
}
```

### DELETE /delete/{id}?date=DATE
Deletes an item by its id. After performing this command, a user will not be able to 
get historical information about the item.

Response: HttpStatus.OK 200 means that the request has been handled successfully.
Response: HttpStatus.BAD_REQUEST 400 means that the input document's scheme is invalid
or that there were validation errors, e.g. invalid dates or FOLDER with url or size fields.

```json
{
  "code": 400,
  "message": "Validation Failed"
}
```
Response: HttpStatus.NOT_FOUND 404 means the element is not present in the database.
```json
{
  "code": 404,
  "message": "Item not found"
}
```

### GET /nodes/{id}
Retrieves information about an element by its ID. Information about child elements is also provided.

Response: HttpStatus.OK 200 means that the request has been handled successfully.

```json
{
  "id": "элемент_1_2",
  "url": null,
  "type": "FOLDER",
  "parentId": null,
  "date": "2022-05-28T21:12:01.000Z",
  "size": 12,
  "children": [
    {
      "url": "/file/url1",
      "id": "элемент_1_3",
      "size": 4,
      "date": "2022-05-28T21:12:01.000Z",
      "type": "FILE",
      "parentId": "элемент_1_2"
    },
    {
      "type": "FOLDER",
      "url": null,
      "id": "элемент_1_1",
      "date": "2022-05-26T21:12:01.000Z",
      "parentId": "элемент_1_2",
      "size": 8,
      "children": [
        {
          "url": "/file/url2",
          "id": "элемент_1_4",
          "parentId": "элемент_1_1",
          "date": "2022-05-26T21:12:01.000Z",
          "size": 8,
          "type": "FILE"
        }
      ]
    }
  ]
}
```
Response: HttpStatus.BAD_REQUEST 400 means that the input document's scheme is invalid
or that there were validation errors, e.g. invalid dates or FOLDER with url or size fields.

```json
{
  "code": 400,
  "message": "Validation Failed"
}
```
Response: HttpStatus.NOT_FOUND 404 means the element is not present in the database.
```json
{
  "code": 404,
  "message": "Item not found"
}
```

### GET /updates?date=DATE
Retrieves a list of elements that have been updated within the past 24 hours from date passed in the request.

Response: HttpStatus.OK 200 means that the request has been handled successfully.

```json
{
  "items": [
    {
      "id": "элемент_1_4",
      "url": "/file/url1",
      "date": "2022-05-28T21:12:01.000Z",
      "parentId": "элемент_1_1",
      "size": 234,
      "type": "FILE"
    }
  ]
}
```
Response: HttpStatus.BAD_REQUEST 400 means that the input document's scheme is invalid
or that there were validation errors, e.g. invalid dates or FOLDER with url or size fields.

```json
{
  "code": 400,
  "message": "Validation Failed"
}
```

### GET /node/{id}/history?dateStart=DATE_START&dateEnd=DATE_END
Retrieves history of updates of elements within the period [START_DATE:END_DATE).
If no START_DATE or END_DATE is provided, then all history for the element is provided.

Response: HttpStatus.OK 200 means that the request has been handled successfully.

```json
{
  "items": [
    {
      "id": "элемент_1_4",
      "url": "/file/url1",
      "date": "2022-05-28T21:12:01.000Z",
      "parentId": "элемент_1_1",
      "size": 234,
      "type": "FILE"
    }
  ]
}
```
Response: HttpStatus.BAD_REQUEST 400 means that the input document's scheme is invalid
or that there were validation errors, e.g. invalid dates or FOLDER with url or size fields.

```json
{
  "code": 400,
  "message": "Validation Failed"
}
```

Response: HttpStatus.NOT_FOUND 404 means the element is not present in the database.
```json
{
  "code": 404,
  "message": "Item not found"
}
```

## Miscellaneous
I tried to implement rate limiting by using Bucket4J spring boot starter. 
It is supposed to limit the number of requests to /imports endpoint to 1000 per minute for all
users. 