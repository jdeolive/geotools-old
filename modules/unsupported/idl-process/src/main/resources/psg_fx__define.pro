; ---------------------------------------------------------------------
;  Copyright (c) 2005-2008, ITT Visual Information Solutions. All
;       rights reserved.
;
;  FILE:
;       _idl_fx_toshape.pro
;
;
;  MODIFICATION HISTORY:
;       06/2009,   SL - written
;       
; -----------------------------------------------------------
; 
; Tiff/Geotiff Feature Extraction + vector output as shapefile
; 
; Returns the created Shapefile (string)
; 

; ---------------------------------------------------------------------------
; Generic reporting routine that selects between bridgenotify and command log
; ---------------------------------------------------------------------------
PRO PSG_FX::Report, title, text
  IF (self.debug) THEN PRINT, title, ': ', text $
  ELSE self->NotifyBridge, title, text
END

; --------------------------------------------------------------------------------
; This routine does the heavy lifting. It is really a wrapper around ENVI_FX_DOIT
; --------------------------------------------------------------------------------
FUNCTION PSG_FX::__idl_execute, imageFile, ruleFile, shapeFile, Debug=debug

  COMPILE_OPT IDL2

  debug = KEYWORD_SET(debug) ; controls whether to print or notify bridge
  
  ; --- Error Handler ---
  ; Reports error to bridge and returns null string to indicate failure ---
  CATCH, Err
  IF (Err NE 0) THEN BEGIN
    CATCH, /CANCEL
    self->Report, 'FX ERROR', !ERROR_STATE.MSG
    RETURN, ''
  ENDIF
  
  ; --- Check the input file validity ---
  self->Report, 'FX File Initialisation', 'Opening input files'
  IF (N_ELEMENTS(imageFile) EQ 0) THEN MESSAGE, 'An input image filename must be supplied'
  IF ~FILE_TEST(imageFile) THEN MESSAGE, 'Input file not found'

  IF (~QUERY_TIFF(imageFile)) THEN MESSAGE, 'Input file must be TIFF format'
  
  ; --- Read the image and metadata ---
  img = READ_TIFF(imageFile, GEOTIFF=metadata, CHANNELS=0)
  IF (N_ELEMENTS(metadata) EQ 0) THEN MESSAGE, 'Input image file must have georeferencing information'
  imSize = SIZE(img)
  dims = imSize[0] EQ 3 ? imSize[2:3] : imSize[1:2]
  nb   = imSize[0] EQ 3 ? imSize[1] : 1L
  
  ; Extract the image filename without the extension
  dirname  = FILE_DIRNAME(imageFile)
  basename = FILE_BASENAME(imageFile)
  locn = STRPOS(basename,'.',/REVERSE_SEARCH)
  IF (locn NE -1) THEN basename = STRMID(basename, 0, locn)
  
  ; Check the ruleset filename, if none was specified use a default based on the input image location
  ;IF (N_ELEMENTS(ruleFile) EQ 0) THEN BEGIN ; default rule filename if none is provided
  ;  ruleFile = FILEPATH('fx_default_rules.xml', ROOT=FILE_DIRNAME(imageFile))
  ;  self->Report, 'FX WARNING', 'No rule file specified, attempting to use default' 
  ;ENDIF
  ;IF ~FILE_TEST(ruleFile) THEN MESSAGE, 'Ruleset file not found' 
  
  ; --- Create the output shape filename ---
  self->Report, 'FX Execution', 'Performing Feature Extraction'
  
  IF N_ELEMENTS(shapeFile) EQ 0 THEN $    
    shapeFile = FILEPATH(basename+'.shp', ROOT=dirName)
    
  ; --- Perform feature extraction using my own half-baked approach ---
  
  img = ROTATE(TEMPORARY(img),7)
  
  hist = TOTAL(HISTOGRAM(img), /CUMUL)
  thresh = MIN(WHERE(hist GT (MAX(hist)*0.85)))
  threshImg = TEMPORARY(img) GT thresh
  
  strucElem = REPLICATE(1, 3, 3) 
  threshImg = ERODE(DILATE(TEMPORARY(threshImg), strucElem), strucElem) 
  
  CONTOUR, threshImg, LEVEL=1, PATH_INFO=pathInfo, PATH_XY=pathXY, /PATH_DATA_COORDS 
  nSegs = N_ELEMENTS(pathInfo)
  IF (nSegs EQ 0) THEN MESSAGE, 'Segmentation detected no discrete objects'
  nFound = 0UL
  
  ; Go through the segments checking size and adding to output vector layer
  FOR i = 0, nSegs - 1 DO BEGIN
  
    nVerts = pathInfo[i].n
    line  = [LINDGEN(nVerts), 0] 
    verts = pathXY[*, pathInfo[i].OFFSET + line]
    oROI  = OBJ_NEW('IDLanROI', verts[0,*], verts[1,*]) 
    roiStats = oROI->ComputeGeometry(AREA=area)
    OBJ_DESTROY, oROI
    
    ; If area is greater than 100 pixels, add it to the layer    
    IF (area LT 500) || (area GT 100000) THEN CONTINUE

    ; Initialize the new shape file (containing polygons)
    IF (~OBJ_VALID(oShapeFile)) THEN BEGIN    
      oShapeFile = OBJ_NEW('IDLffShape', shapeFile, /UPDATE, ENTITY_TYPE=5) 
      IF(~OBJ_VALID(oShapeFile)) THEN MESSAGE, 'Failed to create output vector file'
    ENDIF
    
    ; --- Convert the coords to map projection ---
    verts[0,*] = verts[0,*] * metadata.ModelPixelScaleTag[0] + (metadata.ModelTiePointTag[3])
    verts[1,*] = verts[1,*] * metadata.ModelPixelScaleTag[1] + (metadata.ModelTiePointTag[4] - (dims[1] * metadata.ModelPixelScaleTag[1])) 
    
    ;--- Create the SHAPE file entity ---
    xMin = MIN(verts[0,*], MAX=xMax)
    yMin = MIN(verts[1,*], MAX=yMax)
    entNew = {IDL_SHAPE_ENTITY} 
    entNew.SHAPE_TYPE = 5 
    entNew.BOUNDS     = [xMin, yMin, 0.0, 0.0, xMax, yMax, 0.0, 0.0]
    entNew.N_VERTICES = N_ELEMENTS(verts[0,*])
    entNew.VERTICES   = PTR_NEW(verts, /NO_COPY)
    entnew.MEASURE    = PTR_NEW(FLTARR(nVerts), /NO_COPY) 
     
    ; Add the new entity to new shapefile. 
    oShapeFile->PutEntity, entNew          

    PTR_FREE, entNew.VERTICES
    PTR_FREE, entNew.MEASURE

    nFound++
     
  ENDFOR 
 
  ; Finish defining and close the new SHAPE file
  oShapeFile->Close 
  OBJ_DESTROY, oShapeFile
 
  IF (nFound EQ 0) THEN MESSAGE, 'No features found in image'
  self->Report, 'FX Complete', 'Processing completed successfully'
  RETURN, shapeFile
 
END

FUNCTION PSG_FX::INIT, DEBUG=debug
  self.debug = KEYWORD_SET(debug)
  RETURN, self->IDLitComponent::INIT()
END

; ---------------------------------------------------------------------------
; CLASS DEFINITION
; ---------------------------------------------------------------------------
PRO PSG_FX__Define
  struct = {PSG_FX, debug:0B, INHERITS IDLitComponent}
END
 
; ---------------------------------------------------------------------------
; TEST PROGRAM (to be called from the ENVI command line
; ---------------------------------------------------------------------------  
PRO PSG_FX_Test

  ; Figure out the names for the test input data files
  dir = 'C:\Users\daniele\Downloads\'
  dir1 = 'D:\work\programs\ITT\IDL71\lib\'
  imageFile = FILEPATH('7667112720090922T154410801.tiff', Root=dir)
  ruleFile  = FILEPATH('ruleset_fx_roofs.xml', Root=dir)
 ; shapefile = FILEPATH('METADATA.shp', Root=dir)

  obj = OBJ_NEW('PSG_FX', /DEBUG)
  result = obj->__idl_execute(imageFile, ruleFile)  
  HELP, result

END
 
 