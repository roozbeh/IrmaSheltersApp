//
//  ViewController.swift
//  IrmaShelters
//
//  Created by Roozbeh Zabihollahi on 9/9/17.
//  Copyright © 2017 iPronto Systems. All rights reserved.
//

import UIKit
import MapKit

class ViewController: UIViewController, MKMapViewDelegate {

    @IBOutlet weak var mapView: MKMapView!
    var locationManager = CLLocationManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        self.mapView.delegate =  self;
        self.mapView.showsScale = true
        self.mapView.showsCompass = true
        
        checkLocationAuthorizationStatus()
        
        getShelters()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
         self.navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        self.navigationController?.isNavigationBarHidden = false
    }
    
    func checkLocationAuthorizationStatus() {
        if CLLocationManager.authorizationStatus() == .authorizedWhenInUse {
            mapView.showsUserLocation = true
        } else {
            locationManager.requestWhenInUseAuthorization()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


    func processShelters(_ shelters: NSArray) {
        for shelter in shelters {
            if let shelter = shelter as? NSDictionary {
                guard let lat = shelter["latitude"] as? CLLocationDegrees else { continue}
                guard let lng = shelter["longitude"] as? CLLocationDegrees else { continue}
                guard let name = shelter["shelter"] as? String else { continue}
                guard let address = shelter["address"] as? String else { continue}
                
                print("Lat: \(lat), Lng: \(lng)")
                
                let shelterAnnotation = Shelter(title: name,
                                      locationName: address,
                                      data: shelter,
                                      coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lng))
                
                mapView.addAnnotation(shelterAnnotation)
            }
        }
    }
    
    func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
#if DEBUG
        let mapRegion = MKCoordinateRegion(center: CLLocationCoordinate2D.init(latitude: 28.5383, longitude: -81.3792) , span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2))
        mapView.setRegion(mapRegion, animated: true)
#else
        let mapRegion = MKCoordinateRegion(center: mapView.userLocation.coordinate, span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2))
        mapView.setRegion(mapRegion, animated: true)
#endif
    }

    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        if let annotation = annotation as? Shelter {
            let identifier = "pin"
            var view: MKPinAnnotationView
            if let dequeuedView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKPinAnnotationView { // 2
                dequeuedView.annotation = annotation
                view = dequeuedView
            } else {
                // 3
                view = MKPinAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                view.canShowCallout = true
                view.calloutOffset = CGPoint(x: -5, y: 5)
                let detailButton = UIButton(type: .detailDisclosure)
                detailButton.addTarget(self, action: #selector(ViewController.showDetails), for: .touchUpInside)
                view.rightCalloutAccessoryView = detailButton as UIView
            }
            return view
        }
        return nil
    }

    func showDetails() {
        self.performSegue(withIdentifier: "showDetail", sender: nil)
    }

    func getShelters() {
        let url = URL(string: "https://irma-api.herokuapp.com/api/v1/shelters")
        let request = URLRequest(url: url!)
        var session = URLSession.shared
        
        let task = session.dataTask(with: request, completionHandler: {data, response, error -> Void in
            print("Response: \(response)")

            do {
                if let jsonResult = try JSONSerialization.jsonObject(with: data!, options: []) as? NSDictionary {
                    if let shelters = jsonResult.value(forKey: "shelters") as? NSArray {
                        self.processShelters(shelters)
                    }
                }
            } catch(let e) {
                print(e)
            }

        })
        
        task.resume()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
        if segue.identifier == "showDetail" {
            if let ctl = segue.destination as? DetailViewController {
                if mapView.selectedAnnotations.count > 0 {
                    if let shelter = mapView.selectedAnnotations[0] as? Shelter {
                        ctl.shelterData = shelter.data
                        if #available(iOS 10.0, *) {
                            ctl.placemark = MKPlacemark(coordinate: mapView.selectedAnnotations[0].coordinate)
                        } else {
                            ctl.placemark = MKPlacemark(coordinate: mapView.selectedAnnotations[0].coordinate, addressDictionary: nil)
                        }
                    }
                }
            }

        }
    }

}

