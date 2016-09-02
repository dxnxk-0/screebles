# Screebles

## API Usage

```javascript
    // POST /api/walls
    // May fail on freemium if more than 5 db created
    // May fail if name is already there
    {
        "name": "mywall"
    }
    
    // POST /api/screebles
    // May fail on freemium if nb records in db are more than 100
    // a screeble is limited to 140 characters | arbitrary json document ?
    {
        "wall": "mywall",
        "lat": 48.880016,
        "lon": 2.327035,
        "screeble": "Dont stay there, there are a lot of weird people around"
    }
    
    // GET /api/screebles?wall=mywall&lat=48.880016&lon=2.327035&radius=10
    [
        "Dont stay there, there are a lot of weird people around"	
    ]
```