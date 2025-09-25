import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter , RouterProvider } from "react-router-dom";
import App from './App.tsx'
import BarcodeButton from './components/BarcodeButton';
import "./style.css";

const router = createBrowserRouter([
    { path: "/", element: <App /> },
    { path: "/scan", element: <BarcodeButton /> },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <RouterProvider router = {router} />
    </React.StrictMode>
)
