import BlobBuilder from "../blob-builder.js";

//Fetch request to extract all people
async function getAllPeople() {
    let data1;
    await fetch("/mvc/extract/all/person", {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

//Ftech request to extract a person with a given id
async function getSinglePerson(id) {
    let data1;
    await fetch("/mvc/extract/person/"+String(id), {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

//fetch request to fetch people in ranges (formatted as: [[i0,i1],[i0,i1]])
async function getAllPeopleInRanges(ranges) {
    let data1;
    await fetch("/mvc/extract/all/person/fromRanges", {
        method: "POST",
        body: JSON.stringify(ranges),
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("export-ranges").addEventListener("click", async () => {
    let ranges = [];
    let body = document.getElementById("exportTableBody");
    while (body.lastElementChild) {
        if (body.lastElementChild.nodeName == "TR") {
            const row = body.lastElementChild;
            if (row.firstElementChild.nodeName == "TD") {
                const col = row.firstElementChild;
                if (col.firstElementChild.firstElementChild.nodeName == "INPUT"
                    && col.firstElementChild.lastElementChild.nodeName == "INPUT") {
                    let value1 = Number(col.firstElementChild.firstElementChild.value||0);
                    let value2 = Number(col.lastElementChild.lastElementChild.value||0);
                    ranges.push([value1,value2]);
                }
            }
        }
        body.lastElementChild.remove();
    }
    console.log(ranges);
    let content = await getAllPeopleInRanges(ranges);
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("persons");
})

document.getElementById("export-selected").addEventListener("click", async () => {
    let ranges = [];
    document.getElementsByName("export-select").forEach((input)=>{
        if(input.checked){
            let id = Number(input.getAttribute("export-select-id"));
            ranges.push([id,id]);
        }
    })
    console.log(ranges);
    let content = await getAllPeopleInRanges(ranges);
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("persons");
})

document.getElementsByName("export").forEach((button)=>{
    button.addEventListener("click",async ()=>{
        let content = await getSinglePerson(button.getAttribute("export-id"));
        const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
        blob.downloadBlob("person");
    })
})

document.getElementById("export-all").addEventListener("click",async ()=>{
    let content = await getAllPeople();
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("person");
})


document.getElementById("exportTableAddRow").addEventListener("click", () => {
    const row = document.createElement("tr");
    const detail1 = document.createElement("td");
    detail1.innerHTML = `
        <div class="input-group mb-3">
            <input type="number" min="0" class="form-control">
            <span class="input-group-text bg-secondary">to</span>
            <input type="number"  min="0" class="form-control">
        </div>
    `;
    const detail2 = document.createElement("td");
    const button = document.createElement("button");
    button.setAttribute("class", "btn btn-primary btn-sm");
    button.innerText = "-";
    button.addEventListener("click", () => {
        row.remove();
    })

    detail2.appendChild(button);

    row.appendChild(detail1);
    row.appendChild(detail2);

    document.getElementById("exportTableBody").appendChild(row);

})