import React, { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { debounce } from "lodash";

const DirectionControl = () => {
  const [socket, setSocket] = useState(null);
  const [name, setName] = useState(null);
  const [positions, setPositions] = useState({});

  useEffect(() => {
    const socket = new SockJS("/game");
    socket.onopen = () => {
      setSocket(socket);
      console.log("WebSocket Connection Established");
      const name = window.prompt("What's your name?");
      setName(name);
      const enroll = {"type": "ENROLL" , "name" : name}
      socket.send(JSON.stringify(enroll))
    };

    socket.onmessage = (event) => {
      console.log(event.data);
      try {
        const eventData = JSON.parse(event.data);
        console.log(eventData);
        if(eventData.broadcasting === 'position'){
          const positionData = { ...positions, ...eventData };
          delete positionData["broadcasting"];
          setPositions(positionData);
        }
      } catch (error) {
        console.error("Error parsing JSON data:", event.data);
      }
      
    };

    socket.onclose = () => {
      console.log("WebSocket Connection Closed");
    };

    // Clean up the socket on unmount
    return () => {
      if (socket) {
        socket.close();
      }
    };
  }, []);

  useEffect(() => {
    const handleKeyDown = debounce((e) => {
      if (!socket ) return;
      switch (e.key) {
        case "ArrowRight":
          const rightSignal = { type: "MOVE", direction: "right"};
          socket.send(JSON.stringify(rightSignal));
          break;

        case "ArrowLeft":
          const leftSignal = { type: "MOVE", direction: "left"};
          socket.send(JSON.stringify(leftSignal));
          break;

        case "ArrowUp":
          const upSignal = { type: "MOVE", direction: "up"};
          socket.send(JSON.stringify(upSignal));
          break;

        case "ArrowDown":
          const downSignal = { type: "MOVE", direction: "down"};
          socket.send(JSON.stringify(downSignal));
          break;

        default:
          break;
      }
    }, 100);

    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [socket]);

  const getDivStyle = (x, y) => ({
    width: "40px", // 각 칸의 너비 설정
    height: "40px", // 각 칸의 높이 설정
    backgroundColor: "black", // 해당 좌표에 객체가 있으면 파란색, 없으면 옅은 회색 배경
    position: "absolute", // 절대 위치로 설정
    left: `${x * 40}px`, // x 좌표값에 40을 곱하여 위치 지정
    top: `${y * 40}px`, // y 좌표값에 40을 곱하여 위치 지정
    border: "2px solid black", // 검정색 테두리
    transition: "left 0.4s, top 0.4s, background-color 0.4s", // 이동시와 배경색 변경 시 부드럽게 하기 위한 transition 속성
    display: "flex", // 가운데 정렬을 위해 flex 사용
    justifyContent: "center", // 가로 가운데 정렬
    alignItems: "center", // 세로 가운데 정렬
    color: "white", // 글자 색상
  });

  return (
    <div style={{ position: "relative" }}>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(32, 40px)", // 32개의 열, 각 열의 너비는 40px
          gridTemplateRows: "repeat(18, 40px)", // 18개의 행, 각 행의 높이는 40px
          backgroundColor:"gray",
        }}
      >
       {Object.entries(positions).map(([key, { x, y }]) => (
          <div key={key} style={getDivStyle(x, y)}>
            {key}
          </div>
        ))}
      </div>
    </div>
  );

  
};

export default React.memo(DirectionControl);