//
//  DetailViewController.swift
//  IrmaShelters
//
//  Created by Roozbeh Zabihollahi on 9/10/17.
//  Copyright © 2017 iPronto Systems. All rights reserved.
//

import UIKit
import MapKit

class DetailViewController: UIViewController {

    @IBOutlet weak var cmdPhoneNumber: UIButton!
    @IBOutlet weak var lblShelterName: UILabel!
    @IBOutlet weak var lblAddress1: UILabel!
    @IBOutlet weak var lblAddress2: UILabel!
    @IBOutlet weak var lblAccepting: UILabel!
    @IBOutlet weak var lblPets: UILabel!
    @IBOutlet weak var lblCounty: UILabel!

    var shelterData:NSDictionary?
    var placemark : MKPlacemark?

    @IBAction func onGetDirections(_ sender: Any) {
        if let placemark = placemark {
            let mapItem = MKMapItem(placemark: placemark)
            let launchOptions = [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving]
            mapItem.openInMaps(launchOptions: launchOptions)
        }
    }

    @IBAction func onMoreInfo(_ sender: Any) {
        if let source = shelterData?["source"] as? String {
            UIApplication.shared.openURL(URL(string: source)!)
        }
    }

    @IBAction func onPhoneCall(_ sender: Any) {
        if let phone = shelterData?["phone"] as? String {
            guard let number = URL(string: "tel://" + phone) else { return }
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(number)
            } else {
                UIApplication.shared.openURL(number)
            }
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        if let name = shelterData?["shelter"] as? String {
            lblShelterName.text = name
        }
        if let county = shelterData?["county"] as? String {
            lblCounty.text = county
        }
        if let address = shelterData?["address"] as? String {
            lblAddress1.text = address
        }
        
        var city = ""
        var state = ""
        var zip = ""
        
        if let city2 = shelterData?["city"] as? String {
            city = city2
        }
        if let state2 = shelterData?["state"] as? String {
            state = state2
        }
        if let zip2 = shelterData?["zip"] as? String {
            zip = zip2
        }
        lblAddress2.text = "\(city) \(state), \(zip)"
        
        if let phone = shelterData?["phone"] as? String {
            cmdPhoneNumber.setTitle(phone, for: UIControlState.normal)
        }
        if let pets = shelterData?["pets"] as? String {
            lblPets.text = pets
        } else {
            lblPets.text = ""
        }

        if let accepting = shelterData?["accepting"] as? Bool {
            if accepting {
                lblAccepting.text = "ACCEPTING"
            } else {
                lblAccepting.text = "Not accepting"
            }
        }

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
