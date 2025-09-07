import express from "express";
import path from "path"
import { fileURLToPath} from "url"

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const dist = path.join(__dirname, "dist"); // Vite builds output copied beside the server after deploying

app.use(express.static(dist, { index:false }));

// Catch all the routes and always serve index.html, so the client-side router can handle navigation
app.get("*", (_req, res) => {
    res.sendFile(path.join(__dirname, "index.html"));
})

const port = process.env.PORT || 3000;
app.listen(port, () => {
    console.log(`Server started on port ${port}`);
})
