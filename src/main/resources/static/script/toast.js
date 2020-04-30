
    var toasting= {
        template: ' <transition v-bind:style="demo">\n' +
            '        <div v-bind:style="toast" v-show="show">\n' +
            '            {{msg}}\n' +
            '        </div>\n' +
            '    </transition>',
        data() {
            return {
                show:false,
                toast: {
                    position: 'fixed',
                    top: '40%',
                    left: '50%',
                    'margin-left': '-15vw',
                    padding: '2vw',
                    width: '18vw',
                    'font-size': '3vw',
                    color: '#fff',
                    'text-align': 'center',
                    'background-color': 'rgba(0, 0, 0, 0.8)',
                    'border-radius': '5vw',
                    'z-index': '999'
                },
                'demo':{},
                'demo-enter-active': {
                    transition: '0.1s ease-out'
                },
                'demo-leave-active': {
                    transition: '0.1s ease-out'
                },
                'demo-enter': {
                    opacity: 0,
                    transform: 'scale(1.2)'
                },
                '.demo-leave-to': {
                    opacity: 0,
                    transform: 'scale(0.8)'
                }
            }
        },
        props: ['theToast','msg'],
        watch:{
            theToast:{
                handler(newStatus,oldStatus){
                    if(newStatus===true){
                        let _this=this;
                        _this.show=true;
                        setTimeout(()=>{
                            _this.show=false;
                        },2000);
                    }
                },
                immediate:true
            }
        },
        methods:{
            changeShow(){
                this.show=false;
                console.log(this.show);
            }
        }
    };