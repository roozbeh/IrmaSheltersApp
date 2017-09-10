//
//  Shelter.swift
//  IrmaShelters
//
//  Created by Roozbeh Zabihollahi on 9/9/17.
//  Copyright © 2017 iPronto Systems. All rights reserved.
//

import UIKit
import MapKit

class Shelter: NSObject, MKAnnotation {
    let title: String?
    let locationName: String
    let data: NSDictionary
    let coordinate: CLLocationCoordinate2D
    
    init(title: String, locationName: String, data: NSDictionary, coordinate: CLLocationCoordinate2D) {
        self.title = title
        self.locationName = locationName
        self.data = data
        self.coordinate = coordinate
        
        super.init()
    }
    
    var subtitle: String? {
        return locationName
    }
}
