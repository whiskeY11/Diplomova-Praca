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
            <h4>Úprava atribúty {{$item->csv_name}}, entita: {{$item->list_name}}</h4>
        </div>
        <form class="form-ed" method="post" action="{{action("AttributeController@EditAttribute")}}">
            {{--<div class="form-row">
                <div class="form-group col-md-6">
                    <label for="csv_id">Atribúta</label>
                    <select class="form-control" id="csv_id" name="csv_id">
                        @foreach($csv as $row)
                            @if($row->id == $item->csv_id)
                                <option selected="selected" value="{{$row->id}}">{{$row->name}}</option>
                            @else
                                <option value="{{$row->id}}">{{$row->name}}</option>
                            @endif
                        @endforeach
                    </select>
                </div>
            </div>--}}
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="value">Hodnota</label>
                    <input id="value" name="value" type="text" class="form-control" value="{{$item->value}}" required autofocus>
                    <input type="hidden" name="_token" value="{{csrf_token()}}">
                    <input type="hidden" name="id" value="{{$item->id}}">
                    <input type="hidden" name="csv_id" value="{{$item->csv_id}}">
                    <br/>
                    <br/>
                </div>
            </div>
            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("AttributeController@ShowAttributeList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
@endsection("content")
