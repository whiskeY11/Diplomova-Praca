<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Support\Facades\Session;

class Client
{
    public function handle($request, Closure $next)
    {
        if (!Session::exists("token")) {
            return redirect('/');
        }

        return $next($request);
    }
}
