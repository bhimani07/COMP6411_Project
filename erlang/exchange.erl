-module(exchange).
-export([start/0, master_process/0]).

start() ->
      {_,L} = file:consult("calls.txt"),
      Calling_dataset = maps:from_list(L),
      Callers = maps:keys(Calling_dataset), 
      MasterKey = spawn(exchange, master_process,[]),
      
      io:fwrite("~n** Calls to be made **~n~n"),
      maps:fold(
        fun(K, V, ok) ->
          io:format("~p: ~p~n", [K, V])
        end, ok, Calling_dataset),
        io:fwrite("~n"),

      lists:foreach(
        fun(Key) -> 
          calling:new_caller(Key, MasterKey)
        end,
        Callers),
      
      lists:foreach(
        fun(Key) -> 
          #{Key := Vals} = Calling_dataset,
          calling:send(Key,Vals)
        end,
        Callers).
      
  
master_process() ->
      receive
        {Username, Message, From, TimeStamp} ->
          io:format("~p received ~p messege from ~p [~p]~n" , [Username, Message, From, TimeStamp]),
          master_process()
      after 10000 ->
          io:format("~nMaster has received no replies for 10 seconds, ending...~n"),
          exit(done)
      end.




