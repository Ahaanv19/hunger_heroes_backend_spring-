document.getElementsByName("update").forEach((button) => {
    button.addEventListener("click", (e) => {
        e.preventDefault();

        fetch(button.getAttribute("href"), {
            method: "GET",
            credentials: "include",
            cache: "no-cache",
            headers: new Headers({
                "content-type": "application/json"
            })
        }).then(function (response) {
            if (response.status !== 200) {
                alert("GET update page failed");
            }
            else {
                // clear overlay
                const overlay = document.getElementById("overlay-Container");
                while (overlay.lastElementChild) {
                    overlay.removeChild(overlay.lastElementChild);
                }

                response.text().then((text) => {
                    overlay.innerHTML = text;
                })

                document.getElementById("overlay").style.display = "block";
                return;
            }
        })
    })
})