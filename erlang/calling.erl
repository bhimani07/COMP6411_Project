-module(calling).
-export([new_caller/2,send/2,wait_for_message/2,create_caller/2]).

new_caller(UserName, Pid)->
    register(UserName, spawn(?MODULE, create_caller, [UserName, Pid])).

create_caller(UserName, Pid)->
    {A1,A2,A3} = erlang:timestamp(), 
    rand:seed(exs1024, {A1, A2, A3}),
    wait_for_message(UserName, Pid).

send(From, Vals)->
    lists:foreach(
      fun(Val) ->
        Val ! {intro, From, intro}
      end
    ,Vals).
        

wait_for_message(UserName, Pid) ->
    receive
        {intro, From, Message} ->
          {_,_,A3} = erlang:timestamp(), 
          %sleep randomly in between 1 to 100 miliseconds
          timer:sleep(rand:uniform(100)),
          %Send intro Confirmation Message to master
          Pid ! {UserName, Message, From, A3},
          %Send messege to Caller that receiver received reply
          From ! {reply, UserName},
          wait_for_message(UserName, Pid);

        %handle reply messege pf current process(mapped by UserName)
        {reply, Name}  ->
          {_,_,A3} = erlang:timestamp(),
          %sleep randomly in between 1 to 100 miliseconds
          timer:sleep(rand:uniform(100)),
          %Send Reply Confirmation Message to master
          Pid ! {UserName, reply, Name, A3},
          wait_for_message(UserName, Pid)  

    after 5000 ->
          io:format("~nProcess ~p has received no calls for 5 seconds, ending...~n" , [UserName]),
          exit(done)

    end.