For using chat-server or chat-client you need ``` java 8 ``` or latest version.

# Chat
HI! This it's local chat created for small people group who want exchanges broadcast or private messages.
***
## Server starting

You need to create PostgreSQL database with any database name, in database need to create table 'users_tbl' with columns
'login', 'password', 'nickname' all type's 'text' and these columns don't can be is null. Column 'login' and 'nickname'
musts be Primary keys. If you want to use script ↓ you still need to create a DB.

## [Script for create table with constraints](https://github.com/potapovleonid/Chat/raw/master/chat-server/create_table.sql)

### What new in v1.1
Added functional input any database name or IP address, now you can set port for chat server.

## [Download server v1.1](https://github.com/potapovleonid/Chat/raw/master/out/artifacts/chat_server_jar/chat-server.jar)

***

## Info for client
Creating account's user must use only one word input for in any field
>Correct: <br>
>Login: Leonid <br>
>Password: Password <br>
>Nickname: Leonid <br><br>
>Incorrect:<br>
>Login: Leonid 123 <br>
>Password: Password 123 <br>
>Nickname: Leonid 123<br>

When specifying credentials in more than one word, you will see error info about this.

![error one word](https://github.com/potapovleonid/Chat/raw/master/img/error_one_word.jpg)

If login or nickname already use, you also see error info.

![error already use](https://github.com/potapovleonid/Chat/raw/master/img/error_alr_use.jpg)

You can write in PM to another user. 
For this click on user nickname in user list located on the right site of the app.

![select user for pm](https://github.com/potapovleonid/Chat/raw/master/img/send_pm_select_user.jpg)

After that you will see info in title app.

![show info about writing in pm](https://github.com/potapovleonid/Chat/raw/master/img/show_info_about_writing_in_pm.jpg)

Messages which you will get will be marked [PM].

## [Download client v1.1](https://github.com/potapovleonid/Chat/raw/master/out/artifacts/chat_client_jar/chat-client.jar)