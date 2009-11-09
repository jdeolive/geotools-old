;  FILE:
;       ta__define.pro
;
FUNCTION ta::_idl__testarray, strings, values
  asize = N_ELEMENTS(strings)
  vsize = N_ELEMENTS(values)
  output = 'Result is'
  value = 0
  for i=0, asize-1 do begin
    output = output + strings[i]
  endfor
  
  for j=0, vsize-1 do begin
    value = value + values[j]
  endfor
  output = output + ':' + value
  RETURN,  output
END

FUNCTION ta::INIT

   RETURN, 1

END

; -----------------------------------------------------------
; Object definition.
PRO ta__define
  struct = {ta, $
    inherits IDLitComponent, $
    strings: ['test'], $
    values: [1.0] $
  }
END