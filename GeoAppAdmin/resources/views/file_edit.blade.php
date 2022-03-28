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
        <form class="form-ed" method="post" action="{{action("JsonController@EditFile")}}">
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="name">Názov</label>
                    <input id="name" name="name" type="text" class="form-control" value="{{$file->name}}" required autofocus>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="icon">Ikona</label> <br/>
                    <div class="row">
                        <div class="col">
                            <select onchange="changeImage(this)" class="form-control" id="icon" name="icon">
                                @foreach($icons as $row)
                                    @if($row->name == $file->icon)
                                        <option selected="selected" value="{{$row->id}}">{{$row->name}}</option>
                                    @else
                                        <option value="{{$row->id}}">{{$row->name}}</option>
                                    @endif
                                @endforeach
                            </select>
                        </div>
                        <div class="col">
                            <img id="iconPreview" src="" class="responsive">
                        </div>
                    </div>
                    <br/>
                    <label for="city"><b>Mesto</b></label><br/>
                    <select class="form-control" id="city" name="city">
                        @if($file->city_id == -1)
                            <option selected="selected" value="-1">Neurčené</option>
                        @endif
                        @foreach($cities as $row)
                            @if($row->id == $file->city_id)
                                <option selected="selected" value="{{$row->id}}">{{$row->name}}</option>
                            @else
                                <option value="{{$row->id}}">{{$row->name}}</option>
                            @endif
                        @endforeach
                    </select> <br/>
                    <div class="form-check-inline">
                        <label class="form-check-label">
                            <input type="checkbox" class="form-check-input" value="" name="load" {{$file->loaded == 1 ? "checked" : ""}}>Použiť dáta (v databáze)
                        </label>
                    </div>

                    <input type="hidden" name="_token" value="{{csrf_token()}}">
                    <input type="hidden" name="id" value="{{$file->id}}">
                    <br/>
                    <br/>
                </div>
            </div>
            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("JsonController@ShowFileList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
<script>
    var $iconsId= @json($iconsId);

    document.addEventListener("DOMContentLoaded", function() {
        changeImageInitial();
    });

    function changeImage(el) {
        document.getElementById("iconPreview").src = $iconsId[el.value];
    }

    function changeImageInitial() {
        var myOption = document.getElementById('icon').options;
        var myOptionIndex = document.getElementById('icon').selectedIndex;
        var myOptionValue = myOption[myOptionIndex].value;
        document.getElementById("iconPreview").src = $iconsId[myOptionValue];
    }
</script>
@endsection("content")
