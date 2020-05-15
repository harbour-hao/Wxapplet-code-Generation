(function (window, undefined) {
    Vue.component('list-button',{
        template:` <div style="margin-left: 30px">
                <input type="file" value="选择文件" @change="handleAndGetFile"  accept="image/*" >
            <div style="margin-top: 60px">  <button v-on:click="uploadImg" v-bind:style="[antbtn,antbtnred]">上传</button> </div>
       <div style="margin-top: 60px"><button v-on:click="scanImg"  v-bind:style="[antbtn,antbtnred]">自动检测布局</button></div>
        <div style="margin-top: 60px"><button  v-on:click="generate" v-bind:style="[antbtn,antbtnred]">生成代码</button> </div>
        <div style="margin-top: 30px">布局代码</div>
        <textarea style="width:180px;height: 100px" v-model="layout" ></textarea>
        <div>样式代码</div>
        <textarea style="width:180px;height: 100px" v-model="style" ></textarea>
            </div>`,
        props:["head"],
        watch:{
            'head':function (newVal,oldVal) {
                this.Head=newVal;
            }
        },
        data(){
            return{
                Head:this.head,
                filename:"",
                fileBook:{},
                layout:'',
                style:'',
                legal:true,
                msg:'生成失败',
                'antbtn':{
                    'line-height': '1.499',
                    'position': 'relative',
                    'display': 'inline-block',
                    'font-weight': '400',
                    'white-space': 'nowrap',
                    'text-align': 'center',
                    'background-image': 'none',
                    'border': '1px solid transparent',
                    '-webkit-box-shadow': '0 2px 0 rgba(0,0,0,0.015)',
                    'box-shadow': '0 2px 0 rgba(0,0,0,0.015)',
                    'cursor': 'pointer',
                    '-webkit-transition': 'all .3s cubic-bezier(.645, .045, .355, 1)',
                    'transition': 'all .3s cubic-bezier(.645, .045, .355, 1)',
                    '-webkit-user-select': 'none',
                    '-moz-user-select': 'none',
                    '-ms-user-select': 'none',
                    'user-select': 'none',
                    '-ms-touch-action': 'manipulation',
                    'touch-action': 'manipulation',
                    'height': '32px',
                    'padding':' 0 15px',
                    'font-size':' 14px',
                    'border-radius': '4px',
                    'color': 'rgba(0,0,0,0.65)',
                    'background-color': '#fff',
                    'border-color': '#d9d9d9'
                },
                'antbtnred': {
                    'color':'#000',
                    'background-color': '#FF7F00',
                    'border-color': '#000',
                    'text-shadow': '0 -1px 0 rgba(0,0,0,0.12)',
                    '-webkit-box-shadow': '0 2px 0 rgba(0,0,0,0.045)',
                    'box-shadow': '0 2px 0 rgba(0,0,0,0.045)'
                }
            }
        },methods:{
            handleAndGetFile(data){
                this.fileBook=data.target.files[0];
                this.filename=data.target.files[0].name;
            },
            uploadImg(){
                let that=this;
                console.log("uploadImg");
                let formData=new FormData();
                formData.append("file", this.fileBook);
                axios({
                    url: '../upload',   //****: 你的ip地址
                    data: formData,
                    method: 'post',

                }).then(function (res) {
                    console.log(res);
                    if(res.data.code===0){
                        console.log("success");
                        that.$emit("uploadImg",res.data.msg);
                    }
                }).catch(function(err) {
                    console.log( err);
                });
            },
            scanImg(){
                if(this.filename==="")return;
                let that=this;
                let formData=new FormData();
                var load = new Loading();
                load.init();
                load.start();
                formData.append("filename", this.filename);
                axios({
                    url: '../scan',   //****: 你的ip地址
                    data: formData,
                    method: 'post',
                }).then(function (res) {
                    console.log(res);
                    if(res.data.code===0){
                        console.log("scan request success");
                        load.stop();
                        that.$emit("scanImg",res.data.msg);
                    }else{
                        load.stop();
                        that.$emit("generateFail","图片过于复杂，扫描失败");
                        console.log( err);
                    }
                }).catch(function(err) {
                    console.log( err);
                });
            },
            generate(){
                if(this.filename==="")return;
                let that=this;
                let formData=new FormData();
                formData.append("filename",this.filename);
                formData.append("head", JSON.stringify(this.Head));
                axios({
                    url: '../generate',   //****: 你的ip地址
                    data:formData,
                    method: 'post',
                    headers:{
                        'Content-Type':'application/json;charset=utf-8',
                        dateType: 'json'
                    }
                }).then(function (res) {
                    console.log(res);
                    if(res.data.code===0){
                        console.log("generate request success");
                        that.layout="";
                        that.style="";
                        that.layout=res.data.msg.content;
                        that.style=res.data.msg.style;
                    }
                }).catch(function(err) {
                    that.$emit("generateFail","生成失败，有重合");
                    console.log( err);
                });
            }
        }
    })})(window);