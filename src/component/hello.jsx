import React, { useState , useEffect} from "react"
import axios from "axios";


const HelloComponent = () => {
    const [hello , setHello] = useState("")


    useEffect(()=>{
        axios.get('/hello')
            .then((resp) => {
                console.log(resp)
                setHello(resp.data+'name!')
            })
            .catch((error) => {
                console.log(error);
            });
    },[])
    
    return (
        <div>
            <h1>{hello}</h1>
        </div>
    );
}

export default HelloComponent;