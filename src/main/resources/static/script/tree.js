var Tree={
    template:`<ul style="width: 1500px">
                        <div v-for = "(item, index) in folders">
                           <span style='white-space:pre;'>{{item.position}}</span>   
                           <span >·&nbsp;</span>       
                           <span @click="selectNode(item,index)" >{{item.val}}</span>
                        </div>
                        <div v-if="folders.length!==0">
                        <el-button  style='margin-top:20px;' content='删除' :color='color' v-on:deleteSelected='deleteMenuNode'></el-button>
                        </div>
                    </ul>
              `,
    props:["folder","selectedNode"],
    data(){
        return{
            selectedIndex:0,
            folders:this.folder,
            selectedNodes:{},
            color:{
                'color': '#fff',
                'background-color': '#FF5A44',
                'border-color': '#FF5A44',
            },
            selectedStatus:false//是否点击的列表
        }
    },
    watch:{
        'folder':function (newVal,oldVal) {
            this.folders=newVal;
        },
        'selectedNode':function (newVal, oldVal) {
            this.selectedNodes=newVal;
            this.selectedStatus=false;//节点改变了状态也需要变
        }
    },
    methods:{
        selectNode(item,index){
            this.selectedIndex=index;
            this.$emit('selectNode',item.node);
            this.selectedStatus=true;
        },
        deleteMenuNode(node){
            if(this.selectedStatus) {
                if (this.folders.length === 0) return;
                this.$emit('deleteMenuNode', this.folders[this.selectedIndex].node);
                this.folders.splice(this.selectedIndex, 1);
                console.log("deleteMenu" + this.selectedIndex);
            }else {
                if(JSON.stringify(this.selectedNodes) === "{}")return;
                this.$emit('deleteMenuNode', this.selectedNodes);
            }
            this.selectedStatus=false;
        }
    }
};