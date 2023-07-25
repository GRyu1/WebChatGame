import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

function Chat() {
  const [client, setClient] = useState(null);
  const [message, setMessage] = useState("");
  const [socket , setSocket] = useState(null);

  useEffect(() => {
    const socketJS = new SockJS('/ws');
    const clientSockJS = new Client({
      webSocketFactory: () => socketJS,
      debug: (str) => console.log(str),
      onConnect: () => console.log('connected'),
      onDisconnect: () => console.log('disconnected')
    });   // 연결

    clientSockJS.onStompError = (frame) => {
      console.log('Broker reported error: ' + frame.headers['message']);
      console.log('Additional details: ' + frame.body);
    };

    clientSockJS.activate();
    setClient(clientSockJS);
    setSocket(socketJS);
  }, []); // 클라이언트 실행 , 저장

  const sendMessage = () => {
    const chatMessage = { sender: "name", content: message, type: 'CHAT' }; // ChatDto 와 같은 구조의 JSON
    client.publish({ destination: '/app/chat', body: JSON.stringify(chatMessage) });
    
    setMessage('');
    console.log('end');
};

  return (
    <div>
      <input type="text" value={message} onChange={(e) =>{
        setMessage(e.target.value);
        console.log(message);}}
      />
      <button onClick={sendMessage}>Send</button>
    </div>
  );
}

export default Chat;