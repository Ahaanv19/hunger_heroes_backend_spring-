export default class BlobBuilder{
    static fileTypeEnum = Object.freeze({
        text : 0,
        json : 1,
    })

    static fileExtentions

    blob;
    fileExtention;

    constructor(fileTypeEnum,content){
        let fileTypeString; 

        if(!Number.isInteger(fileTypeEnum)){
            console.warn("Not an expected file type. Defaulting to plain text");
        }
        else if(!Object.values(BlobBuilder.fileTypeEnum).includes(fileTypeEnum)){
            console.warn("Not an expected file type. Defaulting to plain text");
        }

        switch(fileTypeEnum){
            case BlobBuilder.fileTypeEnum.text:
                fileTypeString = "text/plain"
                this.fileExtention = ".txt";
                break;
            case BlobBuilder.fileTypeEnum.json:
                fileTypeString = "application/json";
                this.fileExtention = ".json";
                break;
            default:
                fileTypeString = "text/plain"; //defaults to plain text
                this.fileExtention = ".txt"
                break;
        }

        let blob;
        switch(typeof content){
            case "string": 
                blob = new Blob([content],{type:fileTypeString});
                break;
            case "object":
                blob = new Blob([JSON.stringify(content)],{type:fileTypeString}); 
                break;
            default:
                blob = new Blob([content.toString()],{type:fileTypeString});
        }

        this.blob = blob;
    }

    getBlob(){
        return this.blob;
    }

    downloadBlob(fileName){
        //create a link element
        const tempLink = document.createElement("a");
        //get an object url for the blob
        const url = URL.createObjectURL(this.blob);
        //set the link url to the object url
        tempLink.href = url;
        //set the name of the download
        tempLink.download = fileName+this.fileExtention || "generatedBlob.txt";
        //temporarily put the link in the body
        document.body.appendChild(tempLink);
        //click the link (download the file)
        tempLink.click();
        //remove the link
        document.body.removeChild(tempLink);
        //clear the object url
        URL.revokeObjectURL(url);
    }
}