async function importGroups(fileContent) {
    let data;
    await fetch("/api/groups/bulk/create", {
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    })
    .then(response => response.json())
    .then(json => {
        data = json;
    })
    .catch(error => {
        console.error("Import failed:", error);
        alert("Import failed.");
    });
    return data;
}


document.getElementById("import-all-groups").addEventListener("click", async () => {
    const input = document.getElementById("groupAllFileUpload");
    if (input.files.length !== 1) {
        alert("You must upload a file.");
        return;
    }
    let file = input.files[0];
    let text = await file.text();


    try {
        let json = JSON.parse(text);
        if (!Array.isArray(json)) {
            alert("This import is expecting an array.");
            return;
        }
        let result = await importGroups(text);
        console.log("Import result:", result);
        alert("Groups imported successfully.");
    } catch (e) {
        alert("Invalid JSON file.");
        console.error("Invalid JSON:", e);
    }
});




