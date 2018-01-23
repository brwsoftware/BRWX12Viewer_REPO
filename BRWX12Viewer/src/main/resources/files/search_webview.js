var markInstance = new Mark(document.body);
function markText(str) {
	markInstance.unmark({
  	"done": function(){
    	markInstance.mark(str);
    }
  });
}

function removeMark() {
   	markInstance.unmark();
}