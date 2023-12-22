var batchForm = "batchForm";

$('#' + batchForm).validate(getValidFormOptinal({
    rules: {
        batchName: {
            required: true,
        },
    },
    messages:{
        batchName:{
            required: "必須有值",
        },
    }
}));

var vm = new Vue({
    el: '#UI000100',
    data: {
        items: {
            schedule:false,
            singletime:false,
            checkbusinessdate:false,
            enable:false,
        },
        batch:{},
        //星期
        mwChk:[],
        //周
        wmChk:[],
        //月
        mChk:[],
        mList:[],
        //日
        mdChk:[],
        mdList:[],
        groupChk:[],
        groupList:[],
        subsys:[],
        radioType:"",
        detail:"",
        tasks:[],
        taskList:[],
        currentIndex:-1,
        tskId:-1,
        messageType:"info",
        message:"",
    },
    methods:{
        mCheckAll(status){
            if(status === "false"){
                vm.mChk=[];
            }else{
                vm.mList.forEach((item)=>{
                    if(vm.mChk.indexOf(item.value)==-1){
                        vm.mChk.push(item.value)
                    }
                })
            }
        },
        mdCheckAll(status){
            if(status === "false"){
                vm.mdChk=[];
            }else{
                vm.mdList.forEach((item)=>{
                    if(vm.mdChk.indexOf(item.value)==-1){
                        vm.mdChk.push(item.value)
                    }
                })
            }
        },
        groupCheckAll(status){
            if(status === "false"){
                vm.groupChk=[];
            }else{
                vm.groupList.forEach((item)=>{
                    if(vm.groupChk.indexOf(item.value)==-1){
                        vm.groupChk.push(item.value)
                    }
                })
            }
        },
        clertData(){
            vm.items =  {
                schedule:false,
                    singletime:false,
                    checkbusinessdate:false,
                    enable:false,
            };
            vm.batch = {};
            //星期
            vm.mwChk = [];
            //周
            vm.wmChk = [];
            //月
            vm.mChk = [];
            vm.mList = [];
            //日
            vm.mdChk = [];
            vm.mdList = [];
            vm.groupChk = [];
            vm.groupList = [];
            vm.subsys = [];
            vm.tasks = [];
            vm.taskList = [];
            vm.radioType = "";
            vm.detail = "";
            vm.currentIndex = -1;
            vm.tskId = -1;
            vm.messageType = "info";
            vm.message = "";
        },
        startTick(){
            doAjax("", "/batch/UI_000100/details", false, false, function(resp) {
                vm.clertData();
                vm.detail = resp.detail;
                vm.subsys = resp.subsys;
                vm.groupList = resp.startGroup;
                $(".batchSchedule").hide();
                $('.changeTask').hide();
                if(vm.detail === 'update'){
                    $('.changeTask').show();
                    if(resp.date !== null){
                        $('#batchDate').val(resp.date);
                        $('#batchTime').val(resp.time);
                    }else{
                        $('#batchDate').val(new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + new Date().getDate());
                        $('#batchTime').val(("0" + new Date().getHours()).slice(-2) + ":" + ("0" + new Date().getMinutes()).slice(-2));
                    }
                    initDatePicker('batchDate');
                    initDateTimePicker('batchTime', 'HH:mm');
                    vm.mdList = resp.mdList;
                    vm.mList = resp.mList;
                    vm.mChk = resp.batch.batchScheduleMonths.split(',');
                    vm.mwChk = resp.batch.batchScheduleWeekdays.split(',');
                    if(resp.batch.batchScheduleWhickweeks !== null){
                        vm.wmChk = resp.batch.batchScheduleWhickweeks.split(',');
                    }
                    vm.mdChk = resp.batch.batchScheduleMonthdays.split(',');
                    vm.batch = resp.batch;
                    if(vm.batch.batchScheduleType === null){
                        vm.batch.batchScheduleType = " ";
                    } else if(vm.batch.batchScheduleType === 'O'){
                        vm.batch.batchScheduleType = 'M'
                    }
                    if(resp.batch.batchStartgroup !== null){
                        vm.groupChk = resp.batch.batchStartgroup.split(',');
                    }
                    vm.radioType = resp.radioType;
                    vm.tasks = resp.tasks;
                    vm.taskList = resp.taskList;
                    if(vm.batch.batchSchedule === 1){
                        $(".batchSchedule").show();
                        vm.items.schedule = !vm.items.schedule;
                    }
                    if(vm.batch.batchSingletime === 1){
                        vm.items.singletime = !vm.items.singletime;
                    }
                    if(vm.batch.batchCheckbusinessdate === 1){
                        vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
                    }
                    if(vm.batch.batchEnable === 1){
                        vm.items.enable = !vm.items.enable;
                    }
                    if(vm.batch.batchExecuteHostName === null){
                        vm.batch.batchExecuteHostName = " ";
                    }
                }else{
                    vm.batch.batchScheduleType = " ";
                    vm.batch.batchNotifytype = 0;
                    vm.batch.batchEnable = 0;
                    vm.batch.batchSchedule = 0;
                    vm.batch.batchSingletime = 0;
                    vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
                    vm.batch.batchCheckbusinessdate = 1;
                    vm.batch.batchSubsys = '0';
                    vm.batch.batchZone = '   ';
                    vm.batch.batchExecuteHostName = " ";
                }
            });
        },
        taskDetails:function(status){
            vm.currentIndex = status;
            vm.tskId = vm.tasks[status].JOBTASK_TASKID;
        },
        taskStore(i){
            var jsonData = {
                sender: -1,
                jobId: vm.tasks[i].JOBS_JOBID,
                tskId: vm.tasks[i].JOBTASK_TASKID,
            };
            doAjax(jsonData, "/batch/UI_000100/saveTaskClick", false, true, function(resp) {
                if ('undefined' !== typeof resp && resp !== null) {
                    vm.currentIndex = -1;
                    vm.tasks = resp;
                }
            });
        },
        taskCancel(status){
            vm.tasks[status].JOBTASK_TASKID = vm.tskId;
            vm.tskId = -1;
            vm.currentIndex = -1;
        },
        taskDel(status){
            var jsonData = {
                jobId: vm.tasks[status].JOBS_JOBID,
            };
            doAjax(jsonData, "/batch/UI_000100/delTaskClick", false, true, function(resp) {
                if ('undefined' !== typeof resp && resp !== null) {
                    vm.tasks = resp;
                }
            });
        },
        taskInsert(){
            if(vm.tskId === -1){
                showPopover('tskId', '必須輸入資料');
                return;
            }
            var jsonData = {
                sender: vm.tasks.length,
                jobId: "",
                tskId: vm.tskId,
            };
            doAjax(jsonData, "/batch/UI_000100/insertTaskClick", false, true, function(resp) {
                if ('undefined' !== typeof resp && resp !== null) {
                    vm.tskId = -1;
                    vm.tasks = resp;
                }
            });
        },
    },
    mounted:function (){
        this.$nextTick(function(){
            vm.startTick();
        })
    },
});

function check(value){
    if(value === "mw"){
        $('.week').attr("disabled",false);
        $('.day').attr("disabled",true);
    }else{
        $('.week').attr("disabled",true);
        $('.day').attr("disabled",false);
    }
}

function checkItem(item){
    if(item === 'batchSchedule'){
        vm.items.schedule = !vm.items.schedule;
        if(vm.batch.batchSchedule === 1){
            $(".batchSchedule").hide();
            vm.batch.batchSchedule = 0;
        }else{
            if(vm.detail === 'update'){
                $(".batchSchedule").show();
            }
            vm.batch.batchSchedule = 1;
        }
    }else if(item === 'batchSingletime'){
        vm.items.singletime = !vm.items.singletime;
        if(vm.batch.batchSingletime === 1){
            vm.batch.batchSingletime = 0;
        }else{
            vm.batch.batchSingletime = 1;
        }
    }else if(item === 'batchCheckbusinessdate'){
        vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
        if(vm.batch.batchCheckbusinessdate === 1){
            vm.batch.batchCheckbusinessdate = 0;
        }else{
            vm.batch.batchCheckbusinessdate = 1;
        }
    }else if(item === 'batchEnable'){
        vm.items.enable = !vm.items.enable;
        if(vm.batch.batchEnable === 1){
            vm.batch.batchEnable = 0;
        }else{
            vm.batch.batchEnable = 1;
        }
    }
}

$("#btnUpdate").bind('click', function() {
    if(doValidateForm(batchForm)){
        var jobid = 0;
        if(vm.tasks.length > 0){
            jobid = vm.tasks[0].JOBS_JOBID;
        }

        var jsonData = {
            jobId: jobid,
            batch: vm.batch,
            radioType: vm.radioType,
            groupChk: vm.groupChk.toString(),
            mdChk: vm.mdChk.toString(),
            mChk: vm.mChk.toString(),
            wmChk: vm.wmChk.toString(),
            mwChk: vm.mwChk.toString(),
            dateTime: $('#batchDate').val() + " " + $('#batchTime').val(),
        };
        doAjax(jsonData, "/batch/UI_000100/saveClick", false, true, function(resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
                if(resp.message === '新增成功'){
                    vm.startTick();
                }
            }
        });
    }
});

$("#btnWaiver").bind('click', function() {
    vm.startTick();
});

$("#changeTaskOrder").bind('click', function() {
    if(vm.tasks.length > 0){
        for (let i = 0; i < vm.tasks.length; i++) {
            if(vm.tasks[i].JOBS_SEQ === ""){
                showPopover(''+vm.tasks[i].JOBS_JOBID, '必須輸入資料');
                return;
            }
        }
        var jobsSeqs = Array.from(vm.tasks,({JOBS_SEQ})=>JOBS_SEQ);
        var jobsJobIDs = Array.from(vm.tasks,({JOBS_JOBID})=>JOBS_JOBID);
        var taskIDs = Array.from(vm.tasks,({TASK_ID})=>TASK_ID);
        var jsonData = {
            sender : vm.tasks.length,
            jobsSeq : jobsSeqs.toString(),
            jobsJobID : jobsJobIDs.toString(),
            taskID : taskIDs.toString(),
        };
        doAjax(jsonData, "/batch/UI_000100/changeTaskOrder", false, true, function(resp) {
            if ('undefined' !== typeof resp) {
                vm.messageType = resp.messageType;
                vm.message = resp.message;
                if(resp.messageType === 'INFO'){
                    vm.tasks = resp.tasks;
                }
            }
        });
    }
});

$('.jobsSeq').change(function () {
    for (let i = 0; i < vm.tasks.length; i++) {
        if(vm.tasks[i].JOBS_SEQ === ""){
            showPopover(''+vm.tasks[i].JOBS_JOBID, '必須輸入資料');
            return;
        }else{
            hidePopover(''+vm.tasks[i].JOBS_JOBID);
            break;
        }
    }
})