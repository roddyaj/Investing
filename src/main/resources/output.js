function showPop(id) {
	const popup = document.getElementById(id);
	popup.classList.add("show");
}

function hidePop(id) {
	const popup = document.getElementById(id);
	popup.classList.remove("show");
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}
