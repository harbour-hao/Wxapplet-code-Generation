(function (window, undefined) {
    Vue.component('type-button',{
        template:`  <div style="display: flex;flex-direction: column;justify-content: center"> 
            <div @click="changeStyle(0)"><el-button  content='边框' :color='bordercolor' ></el-button></div>
            <div @click="changeStyle(1)"><el-button  content='字体' :color='fontcolor' ></el-button></div>
            <div @click="changeStyle(2)"><el-button  content='图片' :color='piccolor' ></el-button></div>
            <div @click="changeStyle(3)"><el-button  content='实体' :color='blockcolor' ></el-button></div>
            </div>`,
        data(){
            return{
                filename:"你暂未选择任何文件",
                fileBook:{},
                fontcolor:{
                    'color': '#fff',
                    'background-color': '#00E5EE',
                    'border-color': '#00E5EE',
                },
                piccolor:{
                    'color': '#fff',
                    'background-color': '#9400D3',
                    'border-color': '#9400D3',
                },
                blockcolor:{
                    'color': '#fff',
                    'background-color': '#CD8500',
                    'border-color': '#CD8500',
                },
                bordercolor:{
                    'color': '#fff',
                    'background-color': '#00ff00',
                    'border-color': '#00ff00',
                }
            }
        },methods:{
            changeStyle(num){
                this.$emit('changeStyle',num);
            }
        }
    })})(window);