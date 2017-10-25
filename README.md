# RXPlaygroundAthena
Demo app for connecting to Athena. Very crude 2 hr implementation.


Designed to use old obsolete Android 19 BLE api

Shows finding and connecting to an Athena.

Shows sequencing commands so there is no need for a 3rd party BLE Library.

1. Discover services
2. Read the 1st desired item
3. When deisred item is read, go to next, ...
4. When last static item is read, enable notifications/indications
5. Handle indications/notifications in onChanged

Needs property to ensure there is only one connection at time and a whole lot more.
