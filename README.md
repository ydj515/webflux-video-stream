# webflux demo
video streaming 예제 입니다.

## Environment
- java 17 
- kotlin
- springboot3
- gradle
- mongodb

## prerequisite
1. java17 install
2. docker install
3. mongodb video stream 테스트 시 반드시 영상 파일이 mongodb에 존재해야함.<br/>
    - 데이터는 `fs.chunks`, `fs.files` collection으로 구성하며, `fs.files`는 아래와 같이 구성.
   ```json
    {
      "_id": {
        "$oid": "66cf400748ac0ad6bdfa2f7d"
      },
      "filename": "파일명",
      "video_id": "videoId",
      "metadata": {
        "title": "영상제목",
        "description": "영상 설명",
        "published_at": {
          "$date": "2024-08-22T07:10:00.000Z"
        },
        "view_count": 440034,
        "like_count": 1924,
        "comment_count": 905,
        "thumbnail_url": "sample.png",
        "tags": "a, b, c, d, e",
        "channel_id": "channelId",
        "contentType": "video/mp4"
      },
      "chunkSize": 261120,
      "length": {
        "$numberLong": "3822967"
      },
      "uploadDate": {
        "$date": "2024-08-28T15:19:36.137Z"
      }
    }
    ```

## project setup
1. `create application.yml` 파일 생성 후 각자 상황에 맞게 설정
    ```shell
    cp src/main/resources/application-sample.yml src/main/resources/application.yml
    ```
   
## video stream test
`localhost:8080`에 접속 후 video streaming 동작 확인