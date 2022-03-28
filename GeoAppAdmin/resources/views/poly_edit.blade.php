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
            <h4>Úprava súboru</h4>
        </div>
        <form class="form-ed" method="post" action="{{action("PolyController@EditPoly")}}">
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="name">Názov</label>
                    <input id="name" name="name" type="text" class="form-control" value="{{$poly->name}}" required autofocus>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="city"><b>Mesto</b></label><br/>
                    <select class="form-control" id="city" name="city">
                        @if($poly->city_id == -1)
                            <option selected="selected" value="-1">Neurčené</option>
                        @endif
                        @foreach($cities as $row)
                            @if($row->id == $poly->city_id)
                                <option selected="selected" value="{{$row->id}}">{{$row->name}}</option>
                            @else
                                <option value="{{$row->id}}">{{$row->name}}</option>
                            @endif
                        @endforeach
                    </select>
                </div>
            </div>
            <br/>
            <br/>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <div class="form-check-inline">
                        <label class="form-check-label">
                            <input type="checkbox" onclick="disableCheckbox()" class="form-check-input" value="" id="create_json" name="create_json"
                                {{$poly->json_created == 1 ? "checked" : ""}}>Vytvoriť JSON&nbsp;&nbsp; &nbsp;
                        </label>
                        <label class="form-check-label">
                            <input type="checkbox" class="form-check-input" value="" id="load_json" name="load_json"
                                {{$poly->json_created == 1 && $json_loaded == 1 ? "checked" : ""}}
                                {{$poly->json_created == 0 ? "disabled" : ""}}>Použiť dáta (v databáze)
                        </label>
                    </div> <br/>

                    <input type="hidden" name="_token" value="{{csrf_token()}}">
                    <input type="hidden" name="id" value="{{$poly->id}}">
                    <br/>
                </div>
            </div>
            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("PolyController@ShowPolyList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
<script>
    function disableCheckbox() {
        var checkBox = document.getElementById("create_json");
        var loadCheckbox = document.getElementById("load_json");

        if (checkBox.checked === true){
            loadCheckbox.disabled = false;
        } else {
            loadCheckbox.disabled = true;
            loadCheckbox.checked = false;
        }
    }
</script>
@endsection("content")
