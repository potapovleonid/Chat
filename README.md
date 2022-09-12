# Chat
HI! This it's local chat created for small people group who want exchanges broadcast or private messages.
***
## Server starting

You need create PostgreSQL database with DBName 'chat-server' in database need create table 'users_tbl' with columns
'login', 'password', 'nickname' all type's columns 'text' and they don't can is null. Column 'login' and 'nickname'
musts be Primary keys

Login and password to DB must be default 'postgres'

The ability to use another database name and set different table name will be added later

## [Download server](https://github.com/potapovleonid/Chat/raw/master/out/artifacts/chat_server_jar/chat-server.jar)

***

## Info for client
Creating account's user uses only one word input.
>Login: Leonid <br>
>Password: Password <br>
>Nickname: Leonid <br>

When specifying credentials in more than one word, you will see error info about this.

![error one word](https://github.com/potapovleonid/Chat/raw/master/img/error_one_word.jpg)

If login or nickname already use, you also see error info.

![error already use](https://github.com/potapovleonid/Chat/raw/master/img/error_alr_use.jpg)

You can write in PM to another user. 
For this click on user nickname in user list located on the right site of the app

![select user for pm](https://github.com/potapovleonid/Chat/raw/master/img/send_pm_select_user.jpg)

After that you will see info in title app

![show info about writing in pm](https://github.com/potapovleonid/Chat/raw/master/img/show_info_about_writing_in_pm.jpg)

Messages which you will get will be marked [PM]

## [Download client v1.1](https://github.com/potapovleonid/Chat/raw/master/out/artifacts/chat_client_jar/chat-client.jar)