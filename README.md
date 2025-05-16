# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## Server Design

[Sequence diagram](https://sequencediagram.org/index.html?presentationMode=readOnly&shrinkToFit=true#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5T9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYHSQ4AAaz5HRgyQyqRgotGMGACClHDCKAAHtCNIziSyTqDcSpyvyoIycSIVKbCkdLjAFJqUMBtfUZegAKK6lTYAiJW3HXKnbLmcoAFicAGZuv1RupgOTxlMfVBvGUVR07uq3R6wvJpeg+gd0BxMEbmeoHUU7ShymgfAgECG8adqyTVOUQFLMlbaR1GQztMba6djOUFBwOHKBdp2-bO2Oaz2YH33ZkFD4wKlYcBd6kR1XV922VOXbOXUebWDl6dARdswjoci1C2sE-gXWndnrrmYr3FMjxTIee71BAJZoGmqwHH+dYRhg5QAExOE4CYDIBoywaBfTgakkHQbhZZmJwnjeH4gTQOw5IwAAMhA0RJAEaQZFkyDmGyf7lNUdRNK0BjqAkaCYaq9yrE8LxvB8cEhpQbLfs6AFDEBuxfNJSzqQC5w-maDblAgTE8rCjHMai6KxNi96GCuRJrmSFKDuJo72eek4cjA3K8lad7mrZBJniyjlgDue5DpQACS0iuUy7mFJe3lzmFx6LhqzCbhw5hIBqrJ4IYVAakglY2WyXbBRu-YoClEVQNFsXjqyHnTteaAoMkN57n5DZ1kp2ZmTykSqJ+mB9b+xTKVhqk4eUklgUeRGlrN+zySC4acchMBoRhvRTbcanLV8BGLTBh3lggBiVSgWUYDlF0wDyhXICV-m9bpzqUsER7QEgABe11+gGQZoKN71rY6E3ZpQWarYhG1gNGTgAIyYUmqgpsBfSRWgT3FVdN0ELlpEVmRlaYF4vj+AEXgoOgDFMb4zCsekmSYEhzD6SUvHSD69E+vUPrNC0QmqCJ3THVB6Cw4+YPlBL0Gg0CCmc+CMBGfYjMHgtktoNZr12XFFVZdw25HlrEE6w1a4Xp50goCbhgpd1VAdjLSv9Qzu5DSNY1hhD1DOpCw0IFgCF+-A8OoehMA1AA0qTFGU4EkJzvR0IwAA4kBrLM+xbPw9xkO8Rn-NC-YQHi9rCth4UfVy1XUu+-7LsWmr0JZ0m5uEZbS4BTIQWkjA5KhWb8voFb8XsuUGcUp1qXyL3ZUD+uyCxB3aiwhPJrNZns8aggQ9ATAozyFoC+leHdcMe32feyHivPuNAfZuXSYVI00vrTkCNbeh3QVK-QwJ8PTaDbrED+JNyaUSptgHwUBsDcHgFVTOR9c6s3ZoXZ+vFagNDLhXL6FtoKYUAQAOQOiBUiNczju3roQ9AsxSFAQfnpZuqtNxyBQOvWEjDRizDHrrReBtGohRqvwreE4EqeRnswJ2i4L6BTchVdhmQuGAJPOVCRU8rxznXs7V2tdZZIK3JwoCd8vxgyflzQ+aoeQADMICf0KOzKOO1+iAOiiRJ4PDMYrUgRTKiARLD2yMh1AAUhAHkKC1QBB0AgUA0p87f0wVY6olIBItEAZXOholdoIOAEEqAcAIBGSgLBAA6iwSKAsvjuOkGmJG8FIaKUMfwhhQFoqzDyQUopJTmHK1Ya3AAVhEtAXDWnWJQB0mAXTKA9OgHrHqQiHJDwpKIhuAiNFNUkdPWesjz76wUYbQew9VHtJiqeRRmjLzSKiSgPRy5DnCJVNgM+JjRhVDiQU7hZzOmfNmcU+ZFyjlbK0ZSF5HDbn3Nsm7Z85Rwk8nXmYvp4N6xWMAQ9NA9jHER2-i44hZz6lST+YUgFpTZoVKqT6Ym5EoFJ2plAfJEd3SwGANgBBhB4iJBSCzDiSTObOgqDzPmAshbGGlgYmhG5uB4BgJCNEGJkXJLYdKuECyW5902b2FV0gABCm8gWNRtuUaQ3h-D7xgPIWJahWgRDULPXVgiYXAi1cy2VH576+35dmIOI0w5f0jL-JwMd44kyAA)

[Sequence diagram]

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
