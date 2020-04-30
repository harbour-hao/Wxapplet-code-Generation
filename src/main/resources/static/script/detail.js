(function (window, undefined) {
    Vue.component('detail',{
        template:`  <div style="display: flex;flex-direction: column;justify-content: center;align-items: center;margin-left: 20px;margin-top: 20px" >
                      <div style="font-size: 15px">弧度(%)</div>
                      <input style="width: 60px" value="radius" type="number" v-model="radius">
                      <div style="margin-top: 20px">文字内容</div>
                      <textarea style="width: 60px" v-model="content" value="content"></textarea>
                    </div>`,
        props:["node"],
        watch:{
            'node':function (newVal,oldVal) {
                this.selectedNode=newVal;
                this.content=this.selectedNode.val.content;
                this.radius=this.selectedNode.val.radius/5;
            },
            content:function (val) {
                this.selectedNode.val.content=val;
            },
            radius:function (val) {
                this.selectedNode.val.radius=val*5;
            }
        },
        data(){
            return{
                selectedNode:this.node,
                content:'',
                radius:0
            }
        },
        methods:{

        }
    })})(window);