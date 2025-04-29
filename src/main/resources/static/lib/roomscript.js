
    var OBJ;
    var INDEX;
    var table;
    var tableIns;

    $(function(){
    $("#addBtn").click(function () {
        $("#tid").val("");
        document.getElementById("myForm").reset();
        $("#tstate").text("可用");
        $("#submit").text("确认添加");
        $("#submit").attr("onclick","addTable()");
        $("#tidDiv").attr("class","myHidden");
        INDEX=layer.open({
            type: 1
            ,content: $("#editTable")
        });
    });
});
    layui.use('table', function(){
    room = layui.table;
    //第一个实例
    tableIns=room.render({
    id:'demo'
    ,elem: '#demo'
    ,width:585
    ,height: 500
    ,url: '/admin/roomlist' //数据接口
    ,page: true //开启分页
    ,limit: 10
    ,cols: [[ //表头
{field: 'num', title: '自习室编号', width:120, fixed: 'left'}
    ,{field: 'capacity', title: '自习室容量(人)', width:150,sort:true}
    ,{field: 'status', title: '占用状态', width:150,sort:true}
    ,{field: 'starttime', title: '占用起始时间', width:150,sort:true}
    ,{field: 'endtime', title: '占用状结束时间', width:150,sort:true}
    ,{fixed: 'right', width: 165, align:'center', toolbar: '#barDemo'}
    ]]
});

    //监听行工具事件
    room.on('tool(test)', function(obj){ //注：tool 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
    OBJ=obj;
    let data = obj.data //获得当前行数据
    ,layEvent = obj.event; //获得 lay-event 对应的值
    if(layEvent === 'del'){
    let r = confirm('真的删除\"'+data.tid+'号桌\"吗？');
    if(r){
    obj.del(); //删除对应行（tr）的DOM结构
    //layer.close(index);
    //向服务端发送删除指令
    $.ajax({
    type:'post',
    url: RequestURL+"/admin/deleteTable",
    data: {
    "tid": data.tid
},
    success: function(data) {
    alert('删除成功');
    tableIns.reload();
}
});
};
} else if(layEvent === 'edit'){
    $("#tidDiv").removeAttr("class");
    $("#tid").text(data.tid);
    $("#tcap").val(data.tcap);
    $("#submit").text("保存修改");
    $("#submit").attr("onclick","editTable()");
    INDEX=layer.open({
    type: 1
    ,content: $("#editTable")
});

}
});
});

    function addTable() {
    let tcap = $("#tcap").val().trim();
    if(!/^\d{1}$/.test(tcap)){
    alert("只能输入1-9的整数!");
    return false;
}
    $.ajax({
    type:'post',
    url: RequestURL+"/admin/addTable",
    data: {
    "tcap":tcap,
},
    success: function(data) {
    alert('添加成功');
    layer.close(INDEX);
    tableIns.reload();
}
});
}

    function editTable(){
    let tcap = $("#tcap").val().trim();
    if(!/^\d{1}$/.test(tcap)){
    alert("只能输入1-9的整数!");
    return false;
}
    $.ajax({
    type:'post',
    url: RequestURL+"/admin/editTable",
    data: {
    "tid": $("#tid").text(),
    "tcap":tcap
},
    success: function(data) {
    alert('修改成功');
    OBJ.update({
    tcap: tcap
});
    layer.close(INDEX);
}
});
}

