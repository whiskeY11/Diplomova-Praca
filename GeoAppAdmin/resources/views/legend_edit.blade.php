@extends("layouts.app")
@section("content")
    <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    <title>GeoApp</title>
    <link rel="stylesheet" type="text/css" href="{{asset('css/app.css?v=').time()}}">
    <link rel="stylesheet" type="text/css" href="{{asset('css/list.css?v=').time()}}">
</head>
<body>
<div class="col-sm-9">
    <div class="well">
        <div class="table-title">
            <h4>Úprava legendy</h4>
        </div>
        <br/>
        <form class="form-ed" method="post" action="{{action("LegendController@EditLegend")}}">
            <div class="form-row">
                <div class="col-xs-12 col-sm-8 col-md-5">
                    <div class="form-group">
                        <input type="text" id="name" name="name" class="form-control text-ln col-md-9" placeholder="Názov" value="{{$legend->name}}"><br/>
                        <div class="row col-md-13 text-align-center">
                            <div class="col">
                                <label><b>0-10%</b></label>
                                <input type="color" id="zero" name="zero" value="{{$legend->zero}}">
                            </div>
                            <div class="col">
                                <label><b>10-20%</b></label>
                                <input type="color" id="first" name="first" value="{{$legend->first}}">
                            </div>
                            <div class="col">
                                <label><b>20-30%</b></label>
                                <input type="color" id="second" name="second" value="{{$legend->second}}">
                            </div>
                            <div class="col">
                                <label><b>30-40%</b></label>
                                <input type="color" id="third" name="third" value="{{$legend->third}}">
                            </div>
                            <div class="col">
                                <label><b>40-50%</b></label>
                                <input type="color" id="fourth" name="fourth" value="{{$legend->fourth}}">
                            </div>
                        </div>
                        <br/>
                        <div class="row col-md-13 text-align-center">
                            <div class="col">
                                <label><b>50-60%</b></label>
                                <input type="color" id="fifth" name="fifth" value="{{$legend->fifth}}">
                            </div>
                            <div class="col">
                                <label><b>60-70%</b></label>
                                <input type="color" id="sixth" name="sixth" value="{{$legend->sixth}}">
                            </div>
                            <div class="col">
                                <label><b>70-80%</b></label>
                                <input type="color" id="seventh" name="seventh" value="{{$legend->seventh}}">
                            </div>
                            <div class="col">
                                <label><b>80-90%</b></label>
                                <input type="color" id="eight" name="eight" value="{{$legend->eight}}">
                            </div>
                            <div class="col">
                                <label><b>90-100%</b></label>
                                <input type="color" id="ninth" name="ninth" value="{{$legend->ninth}}">
                            </div>
                        </div>

                        <input type="hidden" name="_token" value="{{csrf_token()}}">
                    </div>
                </div>
            </div> <br/>

            <input type="hidden" name="_token" value="{{csrf_token()}}">
            <input type="hidden" name="id" value="{{$legend->id}}">

            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("LegendController@ShowLegendList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
@endsection("content")
