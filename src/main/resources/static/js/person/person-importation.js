//fetch request to import multiple users
async function importAllPeople(fileContent) {
    let data1;
    await fetch("/mvc/import/all/person", {
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

//fetch request to update a person with a given id using a json file
async function importPersonWithId(fileContent, id) {
    let data1;
    await fetch("/mvc/import/person/" + String(id), {
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

//fetch request to update a person with a given id using a json file
async function importPerson(fileContent) {
    let data1;
    await fetch("/mvc/import/person", {
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("import-all").addEventListener("click", async () => {
    const input = document.getElementById("personAllFileUpload");
    if (input.files.length != 1) {
        alert("You must upload a file.")
        return;
    }
    let file = input.files[0];
    let text = await file.text()
    if (!Array.isArray(JSON.parse(text))) {
        alert("This import is expecting an array.")
        return;
    }
    let content = await importAllPeople(text);
})

document.getElementsByName("import").forEach((button) => {
    button.addEventListener("click", async () => {

        let input;
        for (const child of button.parentElement.children) {
            if (child.getAttribute("id") == "personSingleFileUpload") {
                input = child;
            }
        }

        if (input.files.length != 1) {
            alert("You must upload a file.")
            return;
        }
        let file = input.files[0];
        let text = await file.text();
        if (!Object.keys(JSON.parse(text)).includes("uid")) {
            alert("This import is expecting an Object with the key of \"uid\".")
            return;
        }
        if (!Object.keys(JSON.parse(text)).includes("email")) {
            alert("This import is expecting an Object with the key of \"email\".")
            return;
        }
        let content = await importPersonWithId(text, button.getAttribute("import-id"));
    })
})

document.getElementById("import-single").addEventListener("click", async () => {
    const input = document.getElementById("personSingleFileUpload");
    if (input.files.length != 1) {
        alert("You must upload a file.")
        return;
    }
    let file = input.files[0];
    let text = await file.text()
    if (!Object.keys(JSON.parse(text)).includes("uid")) {
        alert("This import is expecting an Object with the key of \"uid\".")
        return;
    }
    if (!Object.keys(JSON.parse(text)).includes("email")) {
        alert("This import is expecting an Object with the key of \"email\".")
        return;
    }
    let content = await importPerson(text);
})