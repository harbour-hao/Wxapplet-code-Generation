(function (window, undefined) {
    Vue.component('el-button',{
        template:'  <div style="width: 60px;float: left;margin-left: 20px;margin-top: 20px">' +
            '<button v-on:click="deleteSelected" v-bind:style="[antbtn,antbtnred,btncolor]">{{content}}</button></div>',
        props:["content","color"],
        watch:{
            'color':function (newVal,oldVal) {
                this.btncolor=newVal;
            }
        },
        data(){
            return{
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
                    'text-shadow': '0 -1px 0 rgba(0,0,0,0.12)',
                    '-webkit-box-shadow': '0 2px 0 rgba(0,0,0,0.045)',
                    'box-shadow': '0 2px 0 rgba(0,0,0,0.045)'
                },
                btncolor:this.color
            }
    },methods:{
            deleteSelected(){
                console.log("inner");
                this.$emit('deleteSelected',"1");
            }
        }
})})(window);