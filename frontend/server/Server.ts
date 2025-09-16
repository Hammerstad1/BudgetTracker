import * as express from "express"
import * as path from "path"

const app = express()

const dist = path.join(__dirname, "..", "dist");

app.use(express.static(dist, { index: false }));
app.get("*", (_req, res) => res.sendFile(path.join(dist,"index.html")));

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Server started on port ${port}`));