# LiveKit App Demo

## Configuration

Before launching the app we need to set:
- Websocket URL: [here](app/src/main/java/com/geg/livekitstreamingdemo/manager/LiveKitManager.kt) -> set the `livekitServerUrl` field at row 28
- ENDPOINT for token generation: [here](app/src/main/java/com/geg/livekitstreamingdemo/manager/TokenRequester.java) -> set the `ENDPOINT` field at row 19 (to generate the tokens I used the sandbox available on LiveKit Cloud)
- SANDBOX_ID for token generation: [here](app/src/main/java/com/geg/livekitstreamingdemo/manager/TokenRequester.java) -> set the `SANDBOX_ID` field at row 21

## Logs

The app uses this log tag: `LiveKit-Streaming-Demo`

Every 5 seconds the app checks the Room status and prints it to the logs.

The UI also displays the current status of all Rooms.

## Issue

Step to reproduce:

1. Launch the app and wait until all 6 Rooms are connected (rooms from 1001 to 1006).
2. Turn off Wi-Fi and wait 8-10 seconds, sometimes at least one Room does not receive the `RECONNECTING` event
3. Re-enable Wi-Fi.
4. Check the Room statuses, some Rooms may remain stuck in the `RECONNECTING` state.
5. From my tests, the issue occurs quite often but if it does not, repeat the steps starting from step 1.