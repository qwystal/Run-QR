# Run-QR
An app for Android to scan QR-Codes and play them.

## Description
This app can scan QR-Codes and play the game within. You can find my games [here](https://github.com/qwystal/QR-Codes).

This entire project was just a project for school and I'm not really proud of it, as could have done so much more. But it's my very first program in Java.

The idea came from the video [Can you fit a whole game into a QR code?](https://www.youtube.com/watch?v=ExwqNreocpg) from [MattKC](https://www.youtube.com/@MattKC).

My games could fit games in one QR-Code, but it didn't work, because my library couldn't detect it, so I had to split the QR-Codes up and make two or more QR-Codes instead of one.

I hate designing.

You can easily create your own QR-Codes if you follow the steps.

1. Have a game you want to convert. __It must be written in HTML.__
2. A QR-Code should hold round about 1kb to be scanable. So if your game exceeds 1kb, split it up. Be aware that the limit are nine (9) QR-Codes.
3. At the beginning of each segment of your game, you have to put in the ID. The ID has three (3) character: which QR-Codes this is, how many QR-Codes there are, and a letter to not confuse it with other games. For example 13D. That means, this is QR-Code one (1) of three (3) and it has the letter D. If you want a second game, you can change the letter.
4. Now you just have to convert your strings to QR-Codes, and finished you are.
