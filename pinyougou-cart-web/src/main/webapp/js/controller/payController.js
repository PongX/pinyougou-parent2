app.controller('payController' ,function($scope ,$location,payService){
    //本地生成二维码
    $scope.createNative=function(){
        payService.createNative().success(
            function(response){
                $scope.money= (response.total_fee/100).toFixed(2) ; //金额
                $scope.out_trade_no= response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                //查询支付状态
                queryPayStatus($scope.out_trade_no);
            }
        );
    };

    ////查询支付状态
    queryPayStatus=function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function(response){
                if(response.success){
                    //支付成功跳转到成功页面，同时通过js传参
                    location.href="paysuccess.html#?money="+$scope.money;
                }else{
                    if (response.message=='二维码超时'){
                        $scope.createNative();//重新生成二维码
                    }else {
                        location.href="payfail.html";
                    }
                }
            }
        );
    }

    //获取金额,使用这种方式而不直接用 $scope.money是因为页面跳转了即为刷新了所有，新页面没调用方法$scope.money不会有值
    $scope.getMoney=function(){
        return $location.search()['money'];
    }
});