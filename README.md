# Screeps Kotlin

You can read 'README' from
https://github.com/exaV/screeps-kotlin-starter
or
https://github.com/Jordan-Cottle/screeps-kotlin

### Getting started

需要确保你配置的screepsBranch(如果不为default)在Screeps中存在, 否则是不生效的

    ./gradlew clean
    ./gradlew build
    ./gradlew deploy

Deployment is automated with gradle. 
The branch `default` branch is used unless you [create a branch](https://support.screeps.com/hc/en-us/articles/203852251-New-feature-code-branches) and change the configuration as described below.

需要在根目录下的 `gradle.properties` 文件中添加screeps对应凭据
Credentials must be provided in a `gradle.properties` file in the root folder of the project or in `$HOME/.gradle`
    
    screepsUser=<your-username>
    screepsPassword=<your-password>
    screepsHost=https://screeps.com (optional)
    screepsBranch=my-branch (optional)

Alternatively, you can set up an [auth token](https://screeps.com/a/#!/account/auth-tokens) instead of a password (only for official servers)

    screepsToken=<your-token>
    screepsHost=https://screeps.com (optional)
    screepsBranch=kotlin-start (optional)

### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types