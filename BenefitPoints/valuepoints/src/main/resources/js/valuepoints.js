//-----------Return tab dialog scripts----------//
// Shows dialog for adding new Return
AJS.$("#add-return-button").click(function(e) {
    e.preventDefault();
    AJS.dialog2("#add-return-dialog").show();
});

AJS.$("#return-dialog-close-button").click(function (e) {
    e.preventDefault();
    AJS.dialog2("#add-return-dialog").hide();
});

AJS.$("#return-delete-dialog-cancel").click(function (e) {
    e.preventDefault();
    AJS.dialog2("#delete-return-cancel").hide();
});

AJS.$('a.dialog').click(function (e) {
    e.preventDefault();
    var num = this.id;
    var name = this.name;
    if (name === "EditReturn"){
        AJS.dialog2("#return-edit-dialog"+num).show();
    } else if (name === "DeleteReturn"){
        AJS.dialog2("#return-delete-dialog"+num).show();
    } else if (name === "EditObjective"){
        AJS.dialog2("#objective-edit-dialog"+num).show();
    } else if (name === "DeleteObjective") {
        AJS.dialog2("#objective-delete-dialog"+num).show();
    } else {

    }
});

AJS.$('a.dialog-cancel').click(function(e) {
    e.preventDefault();
    var id = this.id;
    var name = this.name;
    if (name == "objective-delete-cancel"){
        AJS.dialog2("#objective-delete-dialog"+id).hide();
    } else if (name == "edit-objective-cancel") {
        AJS.dialog2("#objective-edit-dialog"+id).hide();
    } else if (name == "return-delete-cancel") {
        AJS.dialog2("#return-delete-dialog"+id).hide();
    } else if (name == "edit-return-cancel") {
        AJS.dialog2("#return-edit-dialog"+id).hide();
    } else {

    }
})

//-------------objective tab dialog scripts---------------------------//
AJS.$("#add-objective-button").click(function(e) {
    e.preventDefault();
    AJS.dialog2("#add-objective-dialog").show();
});

AJS.$("#objective-dialog-close-button").click(function (e) {
    e.preventDefault();
    AJS.dialog2("#add-objective-dialog").hide();
});

//-----------------------------------------------------//


//--------------------Other/ Test--------------------------------//






