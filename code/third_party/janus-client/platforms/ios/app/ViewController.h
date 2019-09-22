//
//  ViewController.h
//  app
//
//  Created by Pasquale Boemio on 13/05/2019.
//  Copyright © 2019 Pasquale Boemio. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

@property (nonatomic, retain) IBOutlet UILabel *status;
@property (nonatomic, retain) IBOutlet UIButton *button;
@property (nonatomic, retain) IBOutlet UITextField *host;

- (IBAction)hostFieldDidChange:(id)sender;
- (IBAction)startStopButtonPressed:(id)sender;

@end

