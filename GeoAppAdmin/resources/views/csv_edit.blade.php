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
            <h4>Úprava CSV súboru</h4>
        </div>
        <form class="form-ed" method="post" action="{{action("CsvController@EditCSV")}}">
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="name"><b>Názov</b></label>
                    <input id="name" name="name" type="text" class="form-control" value="{{$csv->name}}" required autofocus>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="legend"><b>Legenda</b></label><br/>
                    <select class="form-control" id="legend" name="legend">
                        @foreach($legends as $row)
                            @if($row->id == $csv->legend_id)
                                <option selected="selected" value="{{$row->id}}">{{$row->name}}</option>
                            @else
                                <option value="{{$row->id}}">{{$row->name}}</option>
                            @endif
                        @endforeach
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="icon">Mesto</label> <br/>
                    <div class="row">
                        <div class="col">
                            <select class="form-control" id="city" name="city">
                                @if($csv->city_id == -1)
                                    <option selected="selected" value="-1">Neurčené</option>
                                @endif
                                @foreach($cities as $row)
                                    @if($row->id == $csv->city_id)
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
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <div class="form-check-inline">
                        <label class="form-check-label">
                            <input type="checkbox" class="form-check-input" value="" name="load" {{$csv->loaded == 1 ? "checked" : ""}}>Použiť atribúty (v databáze)
                        </label>
                    </div>

                    <input type="hidden" name="_token" value="{{csrf_token()}}">
                    <input type="hidden" name="id" value="{{$csv->id}}">
                    <br/>
                    <br/>
                </div>
            </div>
            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("CsvController@ShowCSVList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
@endsection("content")
