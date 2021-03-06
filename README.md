<img align="left" width="33%" src="https://raw.githubusercontent.com/DiogoTheCoder/make-java-great-again/master/MJGA.png" alt="Make Java Great Again Logo">

<div>
    <img src="https://circleci.com/gh/DiogoTheCoder/make-java-great-again.svg?style=svg" alt="CircleCI Badge" />
    <img src="https://dev.azure.com/DiogoTheCoder/Make%20Java%20Great%20Again/_apis/build/status/DiogoTheCoder.make-java-great-again?branchName=master" alt="Azure Build" />
    <img src="https://app.codacy.com/project/badge/Grade/1eb2d5878dd44a12a8a12d7e9fce3e38" alt="Codacy Badge" />
    <img src="https://raw.githubusercontent.com/DiogoTheCoder/make-java-great-again/master/java8+.png" alt="java: &gt;= 8" />
</div>

<br />

_Make Java Great Again! Refactoring to Functional_, is a dissertation-based project supervised by [@BrunelCS](https://github.com/BrunelCS) and [Dr Rumyana Neykova](https://www.brunel.ac.uk/people/rumyana-neykova), who's sole aim is to increase the usage of functional paradigms in Java and developer's confidence in using these paradigms.

<br /><br /><br /><br />

## Features

Currently, this VS Code extension provides code highlighting, quick fixing and refactoring for the following patterns:
*   forEach
*   map
*   reduce

> Code Highlighting
> 
> ![Code Highlighting](https://raw.githubusercontent.com/DiogoTheCoder/make-java-great-again/master/examples/foreach-codehighlight.png)

> Quick Fix
> 
> ![Quick Fix](https://github.com/DiogoTheCoder/make-java-great-again/blob/master/examples/foreach-quickfix.png?raw=true)

> Refactor Entire File
>
> ![Refactor](https://github.com/DiogoTheCoder/make-java-great-again/blob/master/examples/reduce-refactorfile.gif?raw=true)

## Contributing

In order to get this Project running locally on your machine for contributing, you'll need to clone this repo then run the following:

_Language Server_: `yarn build` (this will generate the appropriate `.jar` files, which need compiling after any changes made)

_VS Code Extension_: `yarn compile` or `yarn watch`, preferably just running it via the Debugger on VS Code

Please ensure you are running Java 8, since this was when functional pardadigms were introduced and the extension must be backwards compatible upto Java 8.

### For more information

*   👨‍🏫 [Presentation](https://docs.google.com/presentation/d/1_jPc1FcllnkuTHoz4-MZNqDyj8vIujrTlvCh0h7rGds/edit?usp=sharing)

**Enjoy!**
