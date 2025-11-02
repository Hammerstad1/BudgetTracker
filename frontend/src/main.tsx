import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter , RouterProvider } from "react-router-dom";
import Home from "./components/Home";
import BarcodeButton from './components/BarcodeButton';
import "./style.css";

const router = createBrowserRouter([
    { path: "/", element: <Home /> },
    { path: "/scan", element: <BarcodeButton /> },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <RouterProvider router = {router} />
    </React.StrictMode>
)
